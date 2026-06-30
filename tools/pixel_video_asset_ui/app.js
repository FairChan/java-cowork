const state = {
  mode: "upload",
  lastOutDir: ""
};

const $ = (selector) => document.querySelector(selector);

const refs = {
  form: $("#processForm"),
  tabs: Array.from(document.querySelectorAll(".tab")),
  panels: Array.from(document.querySelectorAll("[data-panel]")),
  file: $("#videoFile"),
  inputPath: $("#inputPath"),
  outDir: $("#outDir"),
  button: $("#processButton"),
  openOutput: $("#openOutputButton"),
  status: $("#statusPill"),
  summary: $("#summaryText"),
  previewFrame: $(".preview-frame"),
  previewImage: $("#previewImage"),
  metrics: $("#metrics"),
  log: $("#logOutput"),
  sheetLink: $("#sheetLink"),
  manifestLink: $("#manifestLink")
};

function setStatus(text, kind = "") {
  refs.status.textContent = text;
  refs.status.className = `status-pill ${kind}`.trim();
}

function setLog(value) {
  refs.log.textContent = value;
}

function collectPayload() {
  return {
    inputPath: refs.inputPath.value.trim(),
    outDir: refs.outDir.value.trim(),
    frameHeight: $("#frameHeight").value,
    fps: $("#fps").value,
    forwardFrames: $("#forwardFrames").value,
    paletteColors: $("#paletteColors").value,
    previewScale: $("#previewScale").value,
    cropPadding: $("#cropPadding").value,
    keepLargestComponent: $("#keepLargestComponent").checked,
    hueMin: $("#hueMin").value,
    hueMax: $("#hueMax").value,
    saturationMin: $("#saturationMin").value,
    valueMin: $("#valueMin").value
  };
}

function payloadToQuery(payload) {
  const query = new URLSearchParams();
  Object.entries(payload).forEach(([key, value]) => query.set(key, value));
  return query.toString();
}

function setMode(mode) {
  state.mode = mode;
  refs.tabs.forEach((tab) => tab.classList.toggle("is-active", tab.dataset.mode === mode));
  refs.panels.forEach((panel) => panel.classList.toggle("hidden", panel.dataset.panel !== mode));
}

function enableLinks(response) {
  refs.sheetLink.href = response.sheetUrl;
  refs.sheetLink.setAttribute("aria-disabled", "false");
  refs.manifestLink.href = response.manifestUrl;
  refs.manifestLink.setAttribute("aria-disabled", "false");
}

function updateMetrics(manifest) {
  const crop = manifest.source_crop;
  const values = [
    manifest.frame_count,
    `${manifest.frame_size.width} x ${manifest.frame_size.height}`,
    manifest.output_fps,
    crop ? `${crop.width} x ${crop.height}` : "-"
  ];
  refs.metrics.querySelectorAll("dd").forEach((node, index) => {
    node.textContent = values[index];
  });
}

function renderResult(response) {
  const stamp = Date.now();
  refs.previewImage.src = `${response.previewUrl}&t=${stamp}`;
  refs.previewFrame.classList.add("has-image");
  refs.summary.textContent = `${response.manifest.frame_count} frames, ${response.manifest.frame_size.width} x ${response.manifest.frame_size.height}`;
  updateMetrics(response.manifest);
  enableLinks(response);
  state.lastOutDir = response.outDir;
  refs.openOutput.disabled = false;
  setLog(JSON.stringify({
    input: response.inputPath,
    output: response.outDir,
    manifest: response.manifest
  }, null, 2));
}

async function processPath(payload) {
  const response = await fetch("/api/process-path", {
    method: "POST",
    headers: {"Content-Type": "application/json"},
    body: JSON.stringify(payload)
  });
  return response.json();
}

async function processUpload(payload) {
  const file = refs.file.files[0];
  if (!file) {
    throw new Error("请选择一个视频文件，或切换到本地路径模式。");
  }
  const response = await fetch(`/api/process-upload?${payloadToQuery(payload)}`, {
    method: "POST",
    headers: {"X-File-Name": file.name},
    body: file
  });
  return response.json();
}

async function handleSubmit(event) {
  event.preventDefault();
  const payload = collectPayload();
  refs.button.disabled = true;
  setStatus("Processing", "is-busy");
  refs.summary.textContent = "正在生成";
  setLog("");

  try {
    const response = state.mode === "upload"
      ? await processUpload(payload)
      : await processPath(payload);
    if (!response.ok) {
      throw new Error(response.error || "处理失败");
    }
    renderResult(response);
    setStatus("Ready");
  } catch (error) {
    setStatus("Error", "is-error");
    refs.summary.textContent = "生成失败";
    setLog(error.message);
  } finally {
    refs.button.disabled = false;
  }
}

async function openOutputDirectory() {
  if (!state.lastOutDir) {
    return;
  }
  await fetch("/api/open-output", {
    method: "POST",
    headers: {"Content-Type": "application/json"},
    body: JSON.stringify({outDir: state.lastOutDir})
  });
}

async function loadPresets() {
  try {
    const response = await fetch("/api/presets");
    const presets = await response.json();
    refs.inputPath.value = presets.defaultInput || "";
    refs.outDir.value = presets.defaultOutput || refs.outDir.value;
  } catch (error) {
    setLog(error.message);
  }
}

refs.tabs.forEach((tab) => {
  tab.addEventListener("click", () => setMode(tab.dataset.mode));
});
refs.form.addEventListener("submit", handleSubmit);
refs.openOutput.addEventListener("click", openOutputDirectory);

loadPresets();
