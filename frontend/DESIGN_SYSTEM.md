# 前端设计系统（Design System Spec）

> 本文件是前端视觉风格的**唯一规范来源**，用于后续页面/项目直接复用，避免风格漂移。
> 实际样式实现在 `src/styles/global.css`（Design Tokens + 共享组件类）。
> 如未来抽出 `<BaseButton>/<BaseCard>/<FormField>` 等基础组件，请以本规范为准。
>
> 风格参照：`https://qoder.com.cn/qoderwork`
> 设计语言：现代浅色 · 圆角卡片 · 柔和分层阴影 · 渐变强调色 · 充足留白（非通用模板，为本项目资产/合同录入场景定制）。

---

## 1. 设计原则

- **浅色为主**：页面背景冷灰，内容承载在白色卡片上。
- **渐变强调**：主品牌色用 `靛蓝(#6366f1) → 青(#06b6d4)` 渐变，仅用于主操作/强调，不滥用。
- **圆角 + 软阴影**：卡片 12px、小组件 8px、按钮/标签用胶囊(pill)；阴影分层、克制。
- **聚焦可见**：所有输入聚焦时用品牌色 + 3px 光环。
- **语义色**：成功/警告/危险各有专属色与极浅底色，用于状态提示。
- **禁用图片内容组件**：不使用 `<img>` 做装饰/预览；文件类信息用"类型色块 + 文字"表达。
- **只实现真实功能**：不堆砌未联调的仪表盘/图表/登录等。

---

## 2. Design Tokens（来自 `src/styles/global.css` 的 `:root`）

| 类别 | 变量 | 值 | 用途 |
|---|---|---|---|
| 品牌渐变 | `--gradient` | `linear-gradient(135deg,#6366f1,#06b6d4)` | 主按钮、Logo、强调条 |
| 品牌渐变(淡) | `--gradient-soft` | `linear-gradient(135deg, rgba(99,102,241,.12), rgba(6,182,212,.12))` | 选中行、聚焦底 |
| 品牌主色 | `--brand` / `--brand-600` / `--brand-700` | `#4f46e5` / `#4338ca` / `#3730a3` | 文字链接、聚焦环、描边 |
| 强调青 | `--accent` | `#06b6d4` | 辅助强调 |
| 背景 | `--bg` / `--bg-soft` / `--surface` / `--surface-2` | `#f4f6fb` / `#eef1f8` / `#fff` / `#f8fafc` | 页面/分区/卡片/卡片内分区 |
| 边框 | `--border` / `--border-strong` | `#e6e9f2` / `#d4d9e6` | 默认/强边框 |
| 文本 | `--text` / `--text-2` / `--text-muted` | `#0f172a` / `#475569` / `#94a3b8` | 标题/正文/弱化 |
| 语义 | `--success` / `--warning` / `--danger` | `#10b981` / `#f59e0b` / `#ef4444` | 成功/警告/危险（均配套 `-bg` 浅底） |
| 圆角 | `--r-lg` / `--r` / `--r-sm` / `--r-pill` | `16` / `12` / `8` / `999` px | 大卡/卡片/小组件/胶囊 |
| 阴影 | `--sh-sm` / `--sh-md` / `--sh-lg` | 见源码 | 卡片/浮层/弹窗 |
| 间距 | `--gap` | `16px` | 区块间距 |

> 扩展新色/新令牌：只在 `:root` 加变量，不要在任何组件里写死十六进制。

---

## 3. 共享组件类（在 `global.css` 中，全局可用）

### 卡片 `.card`
白色背景 + 1px 细边框 + 软阴影 + 12px 圆角。所有内容区块的基础容器。

### 按钮 `.btn`（变体）
- `.btn--primary`：渐变底 + 白字 + 阴影（**主操作**）
- `.btn--ghost`：白底 + 灰边 + 深字，hover 变品牌色（**次操作**）
- `.btn--danger`：白底 + 红边 + 红字，hover 红底（**删除/危险**）
- `.btn--sm`：小尺寸
- 禁用：`.btn:disabled`（透明度降低、禁止点击）
- 圆角为胶囊；按下 `translateY(1px)`。

### 表单字段 `.field`
- 结构：`<div class="field"><label class="field__label">标签</label><input/></div>`
- 整行圆角边框，聚焦时边框变品牌色 + 3px 光环。
- 必填：`<label class="field__label is-required">`，自动显示红色 `*`。
- 用于单行文本/数字/日期/下拉，统一外观。

### 区块标题 `.section-title`
卡片内小标题：左侧 4px 渐变竖条 + 加粗文字，底部细线，右上可放操作按钮（`margin-left:auto`）。

### 徽标 `.badge` / 文件色块 `.filechip`
- `.badge--brand/--pdf/--img/--muted/--success/--warning`：小胶囊标签。
- `.filechip--pdf`（红渐变）/ `.filechip--img`（蓝渐变）：**替代图片预览**的文件类型色块，显示 `PDF`/`IMG` 字样。

### 应用外壳 `.app-shell` / `.app-header` / `.app-main`
- 全屏浅底（带两角径向渐变光晕）。
- 顶部标题栏：Logo(渐变方角) + 标题 + 副标题，右侧放 Toolbar。
- 主区 `max-width:1200px` 居中、纵向 `gap` 排列各卡片。

### 工具类
`.u-muted`（弱文本）、`.u-text-right`、`.u-text-center`、`.u-empty`（空态居中提示）。

---

## 4. 典型用法示例

### 页面外壳
```vue
<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="app-header__inner">
        <div class="app-brand">
          <div class="app-logo">资</div>
          <div>
            <div class="app-title">资产识别录入</div>
            <div class="app-sub">合同识别 · 结构化录入 · 设备入账管理</div>
          </div>
        </div>
        <Toolbar @new="..." @save="..." />
      </div>
    </header>
    <main class="app-main">
      <!-- 各卡片区块 -->
    </main>
  </div>
</template>
```

### 工具栏
```vue
<div class="toolbar">
  <button class="btn btn--primary" @click="onNew">+ 新增</button>
  <button class="btn btn--ghost" @click="onSave">保存</button>
  <button class="btn btn--ghost" @click="onReset">重置</button>
</div>
```

### 卡片 + 区块标题 + 表格
```vue
<div class="card">
  <div class="section-title">
    <span>合同列表</span>
    <button class="btn btn--primary btn--sm" style="margin-left:auto">操作</button>
  </div>
  <div class="table-scroll">
    <table class="list-table">
      <thead><tr><th>编号</th> … </tr></thead>
      <tbody>
        <tr v-for="row in rows" :key="row.id">
          <td class="cell-strong">{{ row.no }}</td> …
        </tr>
        <tr v-if="!rows.length"><td colspan="8" class="u-empty">暂无数据</td></tr>
      </tbody>
    </table>
  </div>
</div>
```

### 表单字段
```vue
<div class="field">
  <label class="field__label is-required">合同编号</label>
  <input type="text" v-model="form.contractNo" placeholder="请输入" />
</div>
```

### 文件上传（不用 `<img>`）
```vue
<div class="file-preview">
  <div class="filechip" :class="isImg ? 'filechip--img' : 'filechip--pdf'">{{ isImg ? 'IMG' : 'PDF' }}</div>
  <div class="file-info">
    <div class="file-name">{{ file.name }}</div>
    <div class="file-size">{{ sizeText }}</div>
  </div>
  <button class="btn btn--ghost btn--sm" @click="clear">清除</button>
  <button class="btn btn--primary btn--sm" @click="recognize">开始识别</button>
</div>
```

### 弹窗（Modal）
```vue
<div class="modal-overlay" v-if="visible" @click.self="close">
  <div class="audit-card">
    <div class="card-header">标题 <button class="btn-close" @click="close">×</button></div>
    <div class="card-body"> …内容… </div>
    <div class="card-footer">
      <button class="btn btn--primary" @click="confirm">保存</button>
      <button class="btn btn--ghost" @click="close">取消</button>
    </div>
  </div>
</div>
```

---

## 5. 复用清单（新页面照做即可）

1. `main.js` 已 `import './styles/global.css'` → 直接使用上述类。
2. 新页面套 `.app-shell` / `.app-main`，内容用 `.card` 分块。
3. 按钮只用 `.btn--primary/--ghost/--danger` 三档。
4. 输入只用 `.field` + `.field__label`。
5. 列表用 `.list-table` 风格；空态用 `.u-empty`。
6. 文件信息用 `.filechip` 而非 `<img>`。
7. 新增视觉令牌 → 只在 `global.css :root` 加变量。

---

## 6. 进阶（推荐但未做）：基础组件化

把上面常用的 `.btn`/`.card`/`.field` 抽成 Vue 基础组件，新页面组合而非记类名：
- `<BaseButton variant="primary|ghost|danger" size="sm">`
- `<BaseCard>`（含可选 `title` 插槽，渲染 `.section-title`）
- `<FormField label="合同编号" required>`（默认插槽放 input）

这样换肤/改风格只动基础组件，杜绝散落样式。是否需要落地，见下方说明。
