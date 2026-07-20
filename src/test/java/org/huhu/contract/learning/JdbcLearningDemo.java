package org.huhu.contract.learning;

import org.huhu.contract.entity.ContractEntity;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ===========================================================
 * JDBC 基础学习 Demo（非 MyBatis 场景）
 * ===========================================================
 *
 * 目的：MyBatis 只是“站在 JDBC 肩膀上”的封装。理解下面这些原语，
 * 才能在使用 MyBatis / 排查慢 SQL / 连接泄漏 / 事务问题时心里有数。
 *
 * 【JDBC 原语 ↔ MyBatis/Spring 抽象 对照表】
 *   Connection（一次连接）        ↔ SqlSession（一次会话）；都线程不安全，用完关
 *   PreparedStatement（? 占位符）  ↔ MyBatis 的 #{} 参数（ParameterHandler + TypeHandler 设置）
 *   ResultSet 逐行映射            ↔ ResultSetHandler（结果集 → Java 对象，含驼峰映射）
 *   conn.commit/rollback          ↔ SqlSession.commit/rollback，以及 Spring 的 @Transactional
 *   DriverManager 直连            ↔ DataSource 连接池（HikariCP/Druid）；MyBatis 从池里借连接
 *   手写 SQL 字符串               ↔ Mapper XML / 注解里的 SQL
 *
 * 【使用注意（生产必看）】
 *   1. 必须关闭 Connection/Statement/ResultSet —— 用 try-with-resources，否则连接泄漏拖垮数据库。
 *   2. 一律用 ? 占位符，绝不字符串拼接 SQL —— 防 SQL 注入（见 demo_sqlInjection）。
 *   3. 自增主键用 Statement.RETURN_GENERATED_KEYS 回写 —— 对应 MyBatis XML 的 useGeneratedKeys。
 *   4. 列名 snake_case(contract_no) → 字段 camelCase(contractNo) 需手动映射，
 *      或开启 mapUnderscoreToCamelCase（你 application.yml 已开）。
 *   5. 事务：要么关自动提交后手动 commit/rollback，要么交给 @Transactional；长事务占连接/锁。
 *   6. 生产用连接池：Connection.close() 是“归还池子”而非真正断开；不要跨请求持有 Connection。
 *
 * 运行前提：本地 MySQL 在 3307 端口、库名 contract_db（与 application.yml 一致）。
 * 下面用的 URL/账号仅演示用，真实项目密码应放配置中心，绝不硬编码。
 */
public class JdbcLearningDemo {

    // 与 application.yml 中 spring.datasource 保持一致（仅演示，生产不要硬编码密码）
    private static final String URL =
            "jdbc:mysql://localhost:3307/contract_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
    private static final String USER = "root";
    private static final String PASS = "eng123";

    /**
     * 1) 原始 JDBC 增 + 查 —— MyBatis 在它之上做了什么
     * 对应业务：ContractService.addContract(contractMapper.insert) 和 getContractById(selectById)。
     */
    @Test
    void demo_jdbcCrud() throws Exception {
        // 获取连接：DriverManager 每次新建“物理连接”，开销大。
        // 生产里 MyBatis 用连接池(HikariCP)的 DataSource 拿连接，用完归还池中。
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            // ---------- 增：PreparedStatement + ? 占位符 ----------
            // ? 占位符 = MyBatis 的 #{}；PreparedStatement 预编译 + 防注入
            String insertSql =
                    "INSERT INTO contracts (contract_no, project_name, party_a, party_b, status) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "HT-JDBC-001");
                ps.setString(2, "JDBC演示项目");
                ps.setString(3, "甲方");
                ps.setString(4, "乙方");
                ps.setInt(5, 0);                       // status TINYINT，用 setInt
                int rows = ps.executeUpdate();
                // 取回自增主键 —— 对应 MyBatis XML 的 useGeneratedKeys="true" keyProperty="id"
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) System.out.println("自增ID=" + keys.getLong(1));
                }
                System.out.println("插入行数=" + rows);
            }

            // ---------- 查：ResultSet → 手动映射成 ContractEntity ----------
            // 这一步就是 MyBatis ResultSetHandler 干的事；驼峰映射靠 mapUnderscoreToCamelCase
            String selectSql = "SELECT id, contract_no, project_name, status FROM contracts WHERE status = ?";
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, 0);                      // 查“待处理”合同
                try (ResultSet rs = ps.executeQuery()) {
                    List<ContractEntity> list = new ArrayList<>();
                    while (rs.next()) {               // 逐行
                        ContractEntity e = new ContractEntity();
                        e.setId(rs.getLong("id"));                  // snake_case → camelCase 手动对应
                        e.setContractNo(rs.getString("contract_no"));
                        e.setProjectName(rs.getString("project_name"));
                        e.setStatus(rs.getInt("status"));
                        list.add(e);
                    }
                    System.out.println("查出合同数=" + list.size());
                }
            }
        }
        // try-with-resources 按 反向顺序 自动关闭 ResultSet → Statement → Connection，避免泄漏
    }

    /**
     * 2) SQL 注入对比 —— 为什么必须用 ? 占位符（对应 MyBatis 的 #{} 而非 ${}）
     */
    @Test
    void demo_sqlInjection() throws Exception {
        String userInput = "1 OR 1=1";               // 假设的恶意输入
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            // ❌ 错误示范（注释）：字符串拼接会让参数变成 SQL 片段
            // String bad = "SELECT * FROM contracts WHERE id = " + userInput;
            // → 实际执行 WHERE id = 1 OR 1=1，全表泄露

            // ✅ 正确：? 占位符，参数被当作“数据”而非“SQL 代码”
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM contracts WHERE id = ?")) {
                ps.setString(1, userInput);          // 被转义处理，不会注入；按类型转换失败则查不到
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("占位符方式安全执行，返回行数由真实参数决定");
                }
            }
            // 经验：MyBatis 里能用 #{} 就绝不用 ${}；${} 只用于非用户输入（如表名/排序字段且白名单校验）。
        }
    }

    /**
     * 3) 事务与隔离级别 —— 对应 Spring 的 @Transactional
     * 你项目里 ContractService 的 addContract/updateContract/deleteContract 标了 @Transactional，
     * Spring 在方法开始 setAutoCommit(false)、正常返回 commit、抛异常 rollback。
     */
    @Test
    void demo_jdbcTransaction() throws Exception {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            conn.setAutoCommit(false);               // 关闭自动提交（MyBatis openSession 默认也如此）
            // 可选：设置隔离级别，对应 @Transactional(isolation = Isolation.READ_COMMITTED)
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO contracts (contract_no, project_name, status) VALUES (?, ?, ?)")) {
                ps.setString(1, "HT-TX-JDBC-1"); ps.setString(2, "事务A"); ps.setInt(3, 0);
                ps.executeUpdate();
                ps.setString(1, "HT-TX-JDBC-2"); ps.setString(2, "事务B"); ps.setInt(3, 0);
                ps.executeUpdate();
                // 两条 INSERT 在同一事务，要么都成要么都败
            }

            conn.commit();                           // 成功提交（Spring 在 @Transactional 方法正常返回时做）
            System.out.println("JDBC 事务提交成功");
            // 异常时调用 conn.rollback() 回滚（Spring 在抛异常时做这件事）

        } catch (Exception e) {
            // 真实代码里在此 conn.rollback(); 再抛出
            throw e;
        }
        // 注意：长事务会一直占着连接和行锁，高并发下易死锁/超时，事务要“短小”。
    }

    /**
     * 4) 连接池视角（概念演示，不实际建池）
     * DriverManager 直连每次都握手建连，代价高；生产用连接池复用连接。
     * MyBatis 的 SqlSession 拿到的 Connection 就来自 Spring 配置的连接池。
     * 注意：从池里拿的 Connection，调用 close() 只是“归还”，不是真正关闭。
     */
    @Test
    void demo_connectionPoolConcept() {
        // 你 application.yml 的数据源由 Spring Boot 自动配成 HikariCP 连接池：
        //   spring.datasource.url / username / password / driver-class-name
        // MyBatis-Plus 的 SqlSessionFactory 持有这个 DataSource；
        // 每次 openSession() → 从池借一个 Connection → 用完归还。
        // 关键注意：
        //   - 永远不要跨请求/跨线程长期持有 Connection 或 SqlSession（线程不安全 + 占池资源）
        //   - 出异常也要保证 close()（用 try-with-resources），否则连接不归还 → 池耗尽
        System.out.println("连接池：借还模型。Connection.close() = 归还，不是断开。");
    }
}
