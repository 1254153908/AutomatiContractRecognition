package org.huhu.contract.learning;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.huhu.contract.entity.ContractEntity;
import org.huhu.contract.mapper.ContractMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * ===========================================================
 * SqlSession 学习 Demo（结合本项目的「合同管理」业务）
 * ===========================================================
 *
 * 【SqlSession 是什么？】
 *   MyBatis 的核心接口，代表与数据库的一次“会话”，相当于 JDBC Connection 的增强版。
 *   线程不安全 —— 每个线程/每次请求都要有自己的 SqlSession，用完必须关闭（try-with-resources）。
 *
 * 【你项目里的真实链路】
 *   业务代码从不直接碰 SqlSession，调用栈是：
 *     ContractController → ContractService(Impl) → ContractMapper(接口)
 *       → MyBatis-Plus 的 BaseMapper → 底层 sqlSession.insert/select/update/delete
 *   MyBatis-Plus 通过 JDK 动态代理为 ContractMapper 生成实现，mapper.xxx() 本质就是 SqlSession 调用。
 *   本 Demo 用最贴近业务的写法演示这条链路，以及事务、批量等原理。
 *
 * 【执行器 ExecutorType（三种，了解即可）】
 *   SIMPLE(默认)：每次执行新建 PreparedStatement，大多数场景用这个。
 *   REUSE       ：相同 SQL 复用 PreparedStatement，减少预编译开销。
 *   BATCH       ：攒一批 SQL 后统一发送，适合大批量 INSERT/UPDATE（见 demo_batchInsert）。
 *
 * 【一级缓存】
 *   同一 SqlSession 内，相同 SQL + 相同参数会命中本地缓存返回同一对象；session.clearCache() 可清除。
 *   Spring 中每个请求一个 SqlSession，缓存作用域很短，一般无需关心。
 *
 * 运行前提：下面手动用 MybatisSqlSessionFactoryBuilder 构建 Factory，仅为“原理演示”。
 * 真实项目里这步由 MyBatis-Plus 的 MybatisPlusAutoConfiguration 基于 application.yml 自动完成，你从不需要手写。
 */
public class SqlSessionLearningDemo {


    private static SqlSessionFactory sqlSessionFactory;

    /** 手动构建 SqlSessionFactory（仅在脱离 Spring 的纯演示/测试里需要） */
    @BeforeAll
    static void init() {
        // 项目真实配置在 application.yml（数据源、mapper 位置、驼峰映射等），由框架自动读取。
        // 这里用最小配置演示“框架在背后做了什么”。
        Environment env = new Environment(
                "demo",
                new JdbcTransactionFactory(),
                new PooledDataSource(
                        "com.mysql.cj.jdbc.Driver",
                        "jdbc:mysql://localhost:3307/contract_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai",
                        "root", "eng123"));
        // 注意：MyBatis-Plus 要用它自己的 MybatisConfiguration（而非原生 Configuration），
        // 否则 BaseMapper 生成的 CRUD SQL 不会被注册。
        MybatisConfiguration config = new MybatisConfiguration(env);
        config.setMapUnderscoreToCamelCase(true);   // 对应 application.yml 的同名开关
        config.addMapper(ContractMapper.class);
        sqlSessionFactory = new MybatisSqlSessionFactoryBuilder().build(config);
    }

    /**
     * 1) getMapper 动态代理 —— 你业务里唯一会用的方式
     * 原理：MyBatis 为 ContractMapper 接口生成代理对象，mapper.xxx() 底层即 sqlSession.xxx()。
     * 等价于你在 ContractService.getContractById 里调用的 contractMapper.selectById(id)。
     */
    @Test
    void demo_getMapper() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ContractMapper mapper = session.getMapper(ContractMapper.class);

            // 下面这行，底层等价于 session.selectOne("org.huhu.contract.mapper.ContractMapper.selectById", 1L)，
            // 但业务里永远用 mapper 接口，不要拼命名空间字符串。
            ContractEntity entity = mapper.selectById(1L);
            System.out.println("查询合同: " + entity);
        }
    }

    /**
     * 2) 完整 CRUD —— 全部走 mapper 接口 + 真实存在的方法
     * 注意 MyBatis 默认不自动提交，演示里手动 commit()；
     * 在你项目里这块由 @Transactional 托管（见 demo_transaction）。
     */
    @Test
    void demo_crud() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ContractMapper mapper = session.getMapper(ContractMapper.class);

            // 增：对应 ContractService.addContract 中的 contractMapper.insert(contract)
            ContractEntity c = new ContractEntity();
            c.setContractNo("HT-DEMO-001");
            c.setProjectName("演示项目");
            c.setPartyA("甲方");
            c.setPartyB("乙方");
            c.setStatus(0);
            mapper.insert(c);                       // 自增主键回写 c.getId()
            System.out.println("插入ID=" + c.getId());

            // 查
            ContractEntity found = mapper.selectById(c.getId());
            System.out.println("查到=" + found.getProjectName());

            // 改：对应 ContractService.updateContract 中的 contractMapper.updateById(contract)
            found.setStatus(1);
            found.setProjectName("演示项目-已识别");
            mapper.updateById(found);

            // 列表：对应 ContractService.listAll() 中的 contractMapper.selectList(null)
            List<ContractEntity> all = mapper.selectList(null);
            System.out.println("合同总数=" + all.size());

            session.commit();                       // 演示用：手动提交；业务里靠 @Transactional
        }
    }

    /**
     * 3) 事务控制 —— 对应 service 层 @Transactional
     * 你项目里 ContractService 的 addContract/updateContract/deleteContract 都标了 @Transactional，
     * 即 Spring 在方法开始时 openSession、异常时 rollback、正常结束 commit。
     * 这里手动演示“同一会话内多条操作要么全成要么全败”。
     */
    @Test
    void demo_transaction() {
        SqlSession session = sqlSessionFactory.openSession();   // 手动提交模式
        try {
            ContractMapper mapper = session.getMapper(ContractMapper.class);

            mapper.insert(sample("HT-TX-001"));
            mapper.insert(sample("HT-TX-002"));
            // 若此处抛异常，上面两条 insert 会一起回滚

            session.commit();
            System.out.println("事务提交成功，两条合同已落库");
        } catch (Exception e) {
            session.rollback();                     // 回滚到事务开始前
            System.err.println("事务回滚: " + e.getMessage());
        } finally {
            session.close();                        // 必须关闭，释放连接
        }
    }

    /**
     * 4) 批量插入 —— 对应 ContractItemMapper.insertBatch（合同明细批量落库）
     * 用 ExecutorType.BATCH 把多次 insert 攒成一批发送，性能远高于逐条提交。
     * 你项目里 ContractService.addContract 调 itemMapper.insertBatch(items) 就是这条链路，
     * 只不过明细用单独的 ContractItemMapper（XML 中 <foreach> 拼接 VALUES）。
     */
    @Test
    void demo_batchInsert() {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            ContractMapper mapper = session.getMapper(ContractMapper.class);
            for (int i = 0; i < 500; i++) {
                mapper.insert(sample("HT-BATCH-" + i));
                if (i % 200 == 0) {
                    session.flushStatements();      // 每 200 条刷一次，控制内存
                }
            }
            session.flushStatements();              // 收尾刷新
            session.commit();
            System.out.println("批量插入 500 条完成");
        }
    }

    /**
     * 5) 一级缓存 —— 同一 SqlSession 内的本地缓存
     */
    @Test
    void demo_firstLevelCache() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ContractMapper mapper = session.getMapper(ContractMapper.class);
            ContractEntity e1 = mapper.selectById(1L);
            ContractEntity e2 = mapper.selectById(1L);
            System.out.println("e1==e2 ? " + (e1 == e2));   // true：命中一级缓存，同一对象

            session.clearCache();
            ContractEntity e3 = mapper.selectById(1L);
            System.out.println("clear 后 e1==e3 ? " + (e1 == e3)); // false：缓存已清，重新查库
        }
    }

    // ======================== 辅助 ========================

    /** 构造一条最小合同样例，便于反复演示 */
    private ContractEntity sample(String contractNo) {
        ContractEntity e = new ContractEntity();
        e.setContractNo(contractNo);
        e.setProjectName("批量项目" + contractNo);
        e.setPartyA("甲方");
        e.setStatus(0);
        return e;
    }
}
