#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
根据模板图结构生成合同管理系统功能模块图 + 3个数据库ER图
运行: python generate_diagrams.py
依赖: matplotlib (pip install matplotlib)
"""
import os
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch

# 配置中文字体
plt.rcParams['font.sans-serif'] = ['Microsoft YaHei', 'SimHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

OUTPUT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'diagrams')
os.makedirs(OUTPUT_DIR, exist_ok=True)


def get_font(size=10, weight='normal'):
    """尝试创建使用中文字体的 FontProperties"""
    try:
        return matplotlib.font_manager.FontProperties(family='Microsoft YaHei', size=size, weight=weight)
    except Exception:
        return matplotlib.font_manager.FontProperties(family='SimHei', size=size, weight=weight)


def draw_rounded_box(ax, x, y, width, height, text, fontsize=9, facecolor='white',
                     edgecolor='#333333', linewidth=1.2, text_color='#333333', radius=0.05,
                     ha='center', va='center'):
    """绘制圆角矩形框"""
    box = FancyBboxPatch((x - width/2, y - height/2), width, height,
                         boxstyle=f"round,pad=0,rounding_size={radius}",
                         facecolor=facecolor, edgecolor=edgecolor, linewidth=linewidth)
    ax.add_patch(box)
    ax.text(x, y, text, fontsize=fontsize, color=text_color, ha=ha, va=va,
            wrap=True, fontproperties=matplotlib.font_manager.FontProperties(fname=None))
    return box


def draw_arrow(ax, x1, y1, x2, y2, color='#555555', lw=1.2):
    """绘制带箭头连线（直线）"""
    ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle='->', color=color, lw=lw,
                              connectionstyle="arc3,rad=0"))


def draw_line(ax, x1, y1, x2, y2, color='#555555', lw=1.2):
    """绘制无箭头直线"""
    ax.plot([x1, x2], [y1, y2], color=color, lw=lw)


def setup_axis(title, figsize=(14, 10)):
    fig, ax = plt.subplots(figsize=figsize)
    ax.set_xlim(0, 14)
    ax.set_ylim(0, 10)
    ax.axis('off')
    ax.set_title(title, fontsize=16, fontweight='bold', pad=15)
    return fig, ax


# ============================================================
# 图1: 合同管理系统功能模块图（仅已实现功能）
# ============================================================
def generate_module_diagram():
    fig, ax = setup_axis('合同管理系统功能模块图（仅已实现）', figsize=(16, 9))
    ax.set_xlim(0, 16)
    ax.set_ylim(0, 9)

    # 中心主题
    center_x, center_y = 8, 4.5
    draw_rounded_box(ax, center_x, center_y, 2.8, 0.8, '合同管理系统',
                     fontsize=14, facecolor='#e0e0e0', edgecolor='#333333', linewidth=2,
                     text_color='#000000', radius=0.08)

    # 左侧一级模块：文件识别
    left_modules = [
        (2.5, 4.5, '文件识别', [
            '上传PDF/图片', 'OCR识别合同', '识别结果回填'
        ]),
    ]

    # 右侧一级模块：合同管理、明细管理、设备入账
    right_modules = [
        (13.5, 7.5, '合同管理', [
            '新增合同', '修改合同', '删除合同',
            '列表查询', '按编号/名称搜索'
        ]),
        (13.5, 4.5, '明细管理', [
            '添加明细行', '删除明细行', '自动计算金额'
        ]),
        (13.5, 1.5, '设备入账', [
            '逐设备录入信息', '保存入账记录',
            '按明细/合同查询'
        ]),
    ]

    # 绘制左侧
    for mx, my, mname, sub_items in left_modules:
        draw_rounded_box(ax, mx, my, 1.6, 0.55, mname, fontsize=11,
                         facecolor='#f5f5f5', edgecolor='#333333', linewidth=1.5, radius=0.06)
        draw_line(ax, mx + 0.8, my, center_x - 1.4, center_y, color='#555555', lw=1.5)
        item_count = len(sub_items)
        start_y = my + (item_count - 1) * 0.55 / 2
        for i, item in enumerate(sub_items):
            iy = start_y - i * 0.55
            draw_rounded_box(ax, mx - 2.0, iy, 1.9, 0.38, item, fontsize=9,
                             facecolor='#ffffff', edgecolor='#666666', linewidth=0.9, radius=0.03)
            draw_line(ax, mx - 1.05, iy, mx - 0.8, my, color='#777777', lw=0.9)

    # 绘制右侧
    for mx, my, mname, sub_items in right_modules:
        draw_rounded_box(ax, mx, my, 1.6, 0.55, mname, fontsize=11,
                         facecolor='#f5f5f5', edgecolor='#333333', linewidth=1.5, radius=0.06)
        draw_line(ax, mx - 0.8, my, center_x + 1.4, center_y, color='#555555', lw=1.5)
        item_count = len(sub_items)
        start_y = my + (item_count - 1) * 0.5 / 2
        for i, item in enumerate(sub_items):
            iy = start_y - i * 0.5
            draw_rounded_box(ax, mx + 2.0, iy, 1.9, 0.38, item, fontsize=9,
                             facecolor='#ffffff', edgecolor='#666666', linewidth=0.9, radius=0.03)
            draw_line(ax, mx + 1.05, iy, mx + 0.8, my, color='#777777', lw=0.9)

    plt.tight_layout()
    path = os.path.join(OUTPUT_DIR, '01_合同管理系统功能模块图.png')
    fig.savefig(path, dpi=200, bbox_inches='tight', facecolor='white')
    plt.close(fig)
    print(f'已生成: {path}')


# ============================================================
# 图2-4: 数据库ER图
# ============================================================
def draw_entity(ax, x, y, name, fields, pk_field=None, fk_fields=None,
                width=2.8, header_height=0.45, row_height=0.32, fontsize=9):
    """绘制ER实体框：表名 + 字段列表"""
    fk_fields = fk_fields or []
    row_count = len(fields)
    height = header_height + row_count * row_height

    # 表头
    header = FancyBboxPatch((x - width/2, y - height/2), width, header_height,
                            boxstyle="round,pad=0,rounding_size=0.05",
                            facecolor='#d9e2f3', edgecolor='#2f5496', linewidth=1.5)
    ax.add_patch(header)
    # 表名
    ax.text(x, y - height/2 + header_height/2, name, fontsize=fontsize+1,
            color='#1f3864', ha='center', va='center', fontweight='bold')

    # 字段区域
    body = FancyBboxPatch((x - width/2, y - height/2 + header_height), width,
                          height - header_height,
                          boxstyle="round,pad=0,rounding_size=0.03",
                          facecolor='#ffffff', edgecolor='#2f5496', linewidth=1.2)
    ax.add_patch(body)

    # 字段名
    for i, (fname, ftype, desc) in enumerate(fields):
        fy = y - height/2 + header_height + row_height/2 + i * row_height
        # 主键/外键标记
        marker = ''
        if pk_field and fname == pk_field:
            marker = 'PK '
        elif fname in fk_fields:
            marker = 'FK '
        label = f'{marker}{fname}: {ftype}'
        ax.text(x - width/2 + 0.12, fy, label, fontsize=fontsize-1,
                color='#333333', ha='left', va='center')

    return height


def draw_relation(ax, x1, y1, x2, y2, label, label_offset=(0, 0.25)):
    """绘制关系连线，两端加 crows foot 风格符号"""
    # 主连线
    ax.plot([x1, x2], [y1, y2], color='#2f5496', lw=1.5)
    # 标签
    lx = (x1 + x2) / 2 + label_offset[0]
    ly = (y1 + y2) / 2 + label_offset[1]
    ax.text(lx, ly, label, fontsize=9, color='#1f3864', ha='center', va='bottom',
            bbox=dict(boxstyle='round,pad=0.2', facecolor='white', edgecolor='none'))


def generate_er_overall():
    fig, ax = setup_axis('合同管理系统整体ER图', figsize=(14, 8))
    ax.set_xlim(0, 14)
    ax.set_ylim(0, 8)

    # contracts
    contract_fields = [
        ('id', 'BIGINT', '主键'),
        ('contract_no', 'VARCHAR(50)', '合同编号'),
        ('project_name', 'VARCHAR(200)', '项目名称'),
        ('party_a', 'VARCHAR(100)', '甲方'),
        ('party_b', 'VARCHAR(100)', '乙方'),
        ('sign_date', 'DATE', '签约日期'),
        ('total_amount', 'DECIMAL(15,2)', '合同总价'),
        ('file_path', 'VARCHAR(255)', 'PDF文件路径'),
        ('status', 'TINYINT', '0待处理/1已识别/2已完成'),
        ('created_at', 'DATETIME', '创建时间'),
    ]
    h1 = draw_entity(ax, 3, 4.5, 'contracts\n合同主表', contract_fields,
                     pk_field='id', width=3.0)

    # contract_items
    item_fields = [
        ('id', 'BIGINT', '主键'),
        ('contract_id', 'BIGINT', '关联合同ID'),
        ('product_name', 'VARCHAR(200)', '产品名称'),
        ('quantity', 'INT', '数量'),
        ('unit_price', 'DECIMAL(15,2)', '单价'),
        ('total_price', 'DECIMAL(15,2)', '合价'),
        ('unit', 'VARCHAR(255)', '单位'),
        ('specification', 'VARCHAR(255)', '规格/型号'),
    ]
    h2 = draw_entity(ax, 8, 4.5, 'contract_items\n合同明细表', item_fields,
                     pk_field='id', fk_fields=['contract_id'], width=3.0)

    # equipment_pending_audit
    audit_fields = [
        ('id', 'BIGINT', '主键'),
        ('item_id', 'BIGINT', '关联合同明细ID'),
        ('lydwh', 'VARCHAR(10)', '使用单位号'),
        ('lydwm', 'VARCHAR(60)', '使用单位名'),
        ('zcbhqj', 'VARCHAR(30)', '设备编号区间'),
        ('zcflh', 'VARCHAR(8)', '分类号'),
        ('zcmc', 'VARCHAR(40)', '设备名称'),
        ('ppxh', 'VARCHAR(30)', '品牌型号'),
        ('gg', 'VARCHAR(50)', '规格'),
        ('sl', 'INT', '数量'),
        ('dj', 'DECIMAL(12,2)', '单价'),
        ('je', 'DECIMAL(12,2)', '金额'),
        ('jldw', 'VARCHAR(20)', '计量单位'),
        ('cj', 'VARCHAR(40)', '厂家'),
        ('ggrq', 'DATE', '购置日期'),
        ('created_at', 'DATETIME', '创建时间'),
    ]
    h3 = draw_entity(ax, 13, 4.5, 'equipment_pending_audit\n设备入账未审核表', audit_fields,
                     pk_field='id', fk_fields=['item_id'], width=3.2)

    # 关系
    draw_relation(ax, 3 + 1.5, 4.5, 8 - 1.5, 4.5, '1 : N')
    draw_relation(ax, 8 + 1.5, 4.5, 13 - 1.6, 4.5, '1 : N')

    plt.tight_layout()
    path = os.path.join(OUTPUT_DIR, '02_整体ER图.png')
    fig.savefig(path, dpi=200, bbox_inches='tight', facecolor='white')
    plt.close(fig)
    print(f'已生成: {path}')


def generate_er_contract_items():
    fig, ax = setup_axis('合同主表与合同明细表ER图', figsize=(12, 7))
    ax.set_xlim(0, 12)
    ax.set_ylim(0, 7)

    contract_fields = [
        ('id', 'BIGINT', '主键'),
        ('contract_no', 'VARCHAR(50)', '合同编号'),
        ('project_name', 'VARCHAR(200)', '项目名称'),
        ('party_a', 'VARCHAR(100)', '甲方'),
        ('party_b', 'VARCHAR(100)', '乙方'),
        ('sign_date', 'DATE', '签约日期'),
        ('total_amount', 'DECIMAL(15,2)', '合同总价'),
        ('file_path', 'VARCHAR(255)', 'PDF文件路径'),
        ('status', 'TINYINT', '状态'),
        ('created_at', 'DATETIME', '创建时间'),
    ]
    draw_entity(ax, 3, 3.5, 'contracts\n合同主表', contract_fields,
                pk_field='id', width=3.0)

    item_fields = [
        ('id', 'BIGINT', '主键'),
        ('contract_id', 'BIGINT', '外键→contracts.id'),
        ('product_name', 'VARCHAR(200)', '产品名称'),
        ('quantity', 'INT', '数量'),
        ('unit_price', 'DECIMAL(15,2)', '单价'),
        ('total_price', 'DECIMAL(15,2)', '合价'),
        ('unit', 'VARCHAR(255)', '单位'),
        ('specification', 'VARCHAR(255)', '规格/型号'),
    ]
    draw_entity(ax, 9, 3.5, 'contract_items\n合同明细表', item_fields,
                pk_field='id', fk_fields=['contract_id'], width=3.0)

    draw_relation(ax, 3 + 1.5, 3.5, 9 - 1.5, 3.5, '1 : N\nON DELETE CASCADE')

    plt.tight_layout()
    path = os.path.join(OUTPUT_DIR, '03_合同主表与明细表ER图.png')
    fig.savefig(path, dpi=200, bbox_inches='tight', facecolor='white')
    plt.close(fig)
    print(f'已生成: {path}')


def generate_er_items_audit():
    fig, ax = setup_axis('合同明细表与设备入账未审核表ER图', figsize=(12, 7))
    ax.set_xlim(0, 12)
    ax.set_ylim(0, 7)

    item_fields = [
        ('id', 'BIGINT', '主键'),
        ('contract_id', 'BIGINT', '关联合同ID'),
        ('product_name', 'VARCHAR(200)', '产品名称'),
        ('quantity', 'INT', '数量'),
        ('unit_price', 'DECIMAL(15,2)', '单价'),
        ('total_price', 'DECIMAL(15,2)', '合价'),
        ('unit', 'VARCHAR(255)', '单位'),
        ('specification', 'VARCHAR(255)', '规格/型号'),
    ]
    draw_entity(ax, 3, 3.5, 'contract_items\n合同明细表', item_fields,
                pk_field='id', fk_fields=['contract_id'], width=3.0)

    audit_fields = [
        ('id', 'BIGINT', '主键'),
        ('item_id', 'BIGINT', '外键→contract_items.id'),
        ('lydwh', 'VARCHAR(10)', '使用单位号'),
        ('lydwm', 'VARCHAR(60)', '使用单位名'),
        ('zcbhqj', 'VARCHAR(30)', '设备编号区间'),
        ('zcflh', 'VARCHAR(8)', '分类号'),
        ('zcmc', 'VARCHAR(40)', '设备名称'),
        ('ppxh', 'VARCHAR(30)', '品牌型号'),
        ('gg', 'VARCHAR(50)', '规格'),
        ('sl', 'INT', '数量'),
        ('dj', 'DECIMAL(12,2)', '单价'),
        ('je', 'DECIMAL(12,2)', '金额'),
        ('jldw', 'VARCHAR(20)', '计量单位'),
        ('cj', 'VARCHAR(40)', '厂家'),
        ('ggrq', 'DATE', '购置日期'),
        ('created_at', 'DATETIME', '创建时间'),
    ]
    draw_entity(ax, 9, 3.5, 'equipment_pending_audit\n设备入账未审核表', audit_fields,
                pk_field='id', fk_fields=['item_id'], width=3.2)

    draw_relation(ax, 3 + 1.5, 3.5, 9 - 1.6, 3.5, '1 : N\nON DELETE CASCADE')

    plt.tight_layout()
    path = os.path.join(OUTPUT_DIR, '04_合同明细表与设备入账ER图.png')
    fig.savefig(path, dpi=200, bbox_inches='tight', facecolor='white')
    plt.close(fig)
    print(f'已生成: {path}')


# ============================================================
# 图5: 合同上传识别流程图（仿登录流程图风格）
# ============================================================
def draw_ellipse(ax, x, y, width, height, text, fontsize=10, facecolor='#ffffff', edgecolor='#333333'):
    """绘制椭圆（开始/结束）"""
    ellipse = mpatches.Ellipse((x, y), width, height, facecolor=facecolor, edgecolor=edgecolor, linewidth=1.5)
    ax.add_patch(ellipse)
    ax.text(x, y, text, fontsize=fontsize, ha='center', va='center', color='#333333')


def draw_rect(ax, x, y, width, height, text, fontsize=10, facecolor='#ffffff', edgecolor='#333333'):
    """绘制矩形（处理步骤）"""
    rect = FancyBboxPatch((x - width/2, y - height/2), width, height,
                          boxstyle="round,pad=0,rounding_size=0.05",
                          facecolor=facecolor, edgecolor=edgecolor, linewidth=1.5)
    ax.add_patch(rect)
    ax.text(x, y, text, fontsize=fontsize, ha='center', va='center', color='#333333')


def draw_diamond(ax, x, y, size, text, fontsize=10, facecolor='#ffffff', edgecolor='#333333'):
    """绘制菱形（判断）"""
    diamond = mpatches.RegularPolygon((x, y), numVertices=4, radius=size,
                                      orientation=0, facecolor=facecolor, edgecolor=edgecolor, linewidth=1.5)
    ax.add_patch(diamond)
    ax.text(x, y, text, fontsize=fontsize, ha='center', va='center', color='#333333')


def draw_flow_arrow(ax, x1, y1, x2, y2, color='#555555', lw=1.2):
    """绘制流程箭头"""
    ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle='->', color=color, lw=lw, connectionstyle="arc3,rad=0"))


def generate_recognition_flow():
    fig, ax = setup_axis('合同上传识别流程图', figsize=(10, 12))
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 12)

    # 流程节点从上到下
    nodes = [
        (5, 11.0, 'start', '开始'),
        (5, 10.0, 'rect', '上传PDF/图片'),
        (5, 8.8, 'diamond', '格式校验\n通过？'),
        (5, 7.6, 'rect', '本地暂存文件'),
        (5, 6.6, 'rect', '上传MinIO'),
        (5, 5.4, 'diamond', 'OCR识别\n成功？'),
        (5, 4.2, 'rect', '解析结构化数据'),
        (5, 3.2, 'rect', '回填合同表单'),
        (5, 2.0, 'end', '结束'),
    ]

    # 绘制节点
    for x, y, ntype, text in nodes:
        if ntype == 'start' or ntype == 'end':
            draw_ellipse(ax, x, y, 1.6, 0.55, text)
        elif ntype == 'rect':
            draw_rect(ax, x, y, 2.2, 0.6, text)
        elif ntype == 'diamond':
            draw_diamond(ax, x, y, 0.75, text)

    # 主流程箭头
    for i in range(len(nodes) - 1):
        x1, y1, _, _ = nodes[i]
        x2, y2, _, _ = nodes[i + 1]
        # 根据节点类型调整连接点
        if nodes[i][2] in ['start', 'end']:
            y1 -= 0.28
        elif nodes[i][2] == 'rect':
            y1 -= 0.3
        elif nodes[i][2] == 'diamond':
            y1 -= 0.65
        if nodes[i+1][2] in ['start', 'end']:
            y2 += 0.28
        elif nodes[i+1][2] == 'rect':
            y2 += 0.3
        elif nodes[i+1][2] == 'diamond':
            y2 += 0.65
        draw_flow_arrow(ax, x1, y1, x2, y2)

    # 校验失败分支
    ax.plot([5 + 0.65, 8.5], [8.8, 8.8], color='#555555', lw=1.2)
    ax.plot([8.5, 8.5], [8.8, 10.0], color='#555555', lw=1.2)
    draw_flow_arrow(ax, 8.5, 10.0, 5 + 1.1, 10.0)
    ax.text(8.7, 8.8, 'N', fontsize=10, color='#333333', ha='left', va='center')
    ax.text(8.7, 9.4, '返回错误提示', fontsize=9, color='#333333', ha='left', va='center')

    # 识别失败分支
    ax.plot([5 + 0.65, 8.5], [5.4, 5.4], color='#555555', lw=1.2)
    ax.plot([8.5, 8.5], [5.4, 6.6], color='#555555', lw=1.2)
    draw_flow_arrow(ax, 8.5, 6.6, 5 + 1.1, 6.6)
    ax.text(8.7, 5.4, 'N', fontsize=10, color='#333333', ha='left', va='center')
    ax.text(8.7, 6.0, '返回错误提示', fontsize=9, color='#333333', ha='left', va='center')

    # Y标签
    ax.text(5.2, 9.15, 'Y', fontsize=10, color='#333333', ha='left', va='center')
    ax.text(5.2, 6.15, 'Y', fontsize=10, color='#333333', ha='left', va='center')

    plt.tight_layout()
    path = os.path.join(OUTPUT_DIR, '05_合同上传识别流程图.png')
    fig.savefig(path, dpi=200, bbox_inches='tight', facecolor='white')
    plt.close(fig)
    print(f'已生成: {path}')


if __name__ == '__main__':
    generate_module_diagram()
    generate_er_overall()
    generate_er_contract_items()
    generate_er_items_audit()
    generate_recognition_flow()
    print(f'\n所有图表已保存到: {OUTPUT_DIR}')
