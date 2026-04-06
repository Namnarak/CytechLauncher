const container = document.getElementById("skin-container");

const getWidth = () => container.clientWidth || window.innerWidth || 300;
const getHeight = () => container.clientHeight || window.innerHeight || 400;

const skinViewer = new skinview3d.SkinViewer({
    canvas: document.createElement("canvas"),
    width: getWidth(),
    height: getHeight(),
    skin: "steve.png"
});

container.appendChild(skinViewer.canvas);

// Default Walking Animation
skinViewer.animation = new skinview3d.WalkingAnimation();
skinViewer.animation.speed = 0.8;

skinViewer.controls.enableRotate = true;
skinViewer.controls.enableZoom = false;
skinViewer.controls.enablePan = false;

// Center the camera and set initial control targets
skinViewer.camera.position.set(0, 10, 50);

// 确保 OrbitControls 也有相同的目标点，覆盖默认的 lookAt
if (skinViewer.controls) {
    skinViewer.controls.update();
} else if (skinViewer.camera.lookAt) {
    skinViewer.camera.lookAt(0, 16, 0);
}

// 保存初始的摄像机位置和控制器目标点（克隆以避免被修改）
const defaultCameraPos = skinViewer.camera.position.clone();
const defaultControlsTarget = skinViewer.controls.target.clone();
let resetAnimationId = null;

// 监听容器的双击事件
container.addEventListener("dblclick", () => {
    // 如果已经在回正动画中，先取消之前的动画
    if (resetAnimationId) {
        cancelAnimationFrame(resetAnimationId);
    }

    const animateReset = () => {
        // Target lerp（直接操作 xyz）
        const t = skinViewer.controls.target;
        const dt = defaultControlsTarget;
        t.x += (dt.x - t.x) * 0.1;
        t.y += (dt.y - t.y) * 0.1;
        t.z += (dt.z - t.z) * 0.1;

        // 当前相机相对 target 的偏移
        const cam = skinViewer.camera.position;
        const ox = cam.x - t.x;
        const oy = cam.y - t.y;
        const oz = cam.z - t.z;

        const dx = defaultCameraPos.x - dt.x;
        const dy = defaultCameraPos.y - dt.y;
        const dz = defaultCameraPos.z - dt.z;

        // 转球坐标
        const curR   = Math.sqrt(ox*ox + oy*oy + oz*oz);
        const defR   = Math.sqrt(dx*dx + dy*dy + dz*dz);
        const curPhi = Math.asin(Math.max(-1, Math.min(1, oy / curR)));
        const defPhi = Math.asin(Math.max(-1, Math.min(1, dy / defR)));
        const curTheta = Math.atan2(oz, ox);
        const defTheta = Math.atan2(dz, dx);

        // 方位角走最短路径
        let dTheta = defTheta - curTheta;
        if (dTheta >  Math.PI) dTheta -= 2 * Math.PI;
        if (dTheta < -Math.PI) dTheta += 2 * Math.PI;

        // 插值
        const nextR     = curR   + (defR   - curR)   * 0.1;
        const nextPhi   = curPhi + (defPhi - curPhi)  * 0.1;
        const nextTheta = curTheta + dTheta * 0.1;

        // 转回笛卡尔坐标
        const cosPhi = Math.cos(nextPhi);
        cam.x = t.x + nextR * cosPhi * Math.cos(nextTheta);
        cam.y = t.y + nextR * Math.sin(nextPhi);
        cam.z = t.z + nextR * cosPhi * Math.sin(nextTheta);

        skinViewer.controls.update();

        // 终止判断
        const dpx = cam.x - defaultCameraPos.x;
        const dpy = cam.y - defaultCameraPos.y;
        const dpz = cam.z - defaultCameraPos.z;
        const distPos = Math.sqrt(dpx*dpx + dpy*dpy + dpz*dpz);

        const ttx = t.x - dt.x;
        const tty = t.y - dt.y;
        const ttz = t.z - dt.z;
        const distTarget = Math.sqrt(ttx*ttx + tty*tty + ttz*ttz);

        if (distPos > 0.05 || distTarget > 0.05) {
            resetAnimationId = requestAnimationFrame(animateReset);
        } else {
            cam.x = defaultCameraPos.x;
            cam.y = defaultCameraPos.y;
            cam.z = defaultCameraPos.z;
            t.x = dt.x; t.y = dt.y; t.z = dt.z;
            skinViewer.controls.update();
            resetAnimationId = null;
        }
    };

    // 启动动画
    animateReset();
});

// 如果用户在回正动画播放时主动拖拽了模型，打断回正动画
if (skinViewer.controls) {
    skinViewer.controls.addEventListener("start", () => {
        if (resetAnimationId) {
            cancelAnimationFrame(resetAnimationId);
            resetAnimationId = null;
        }
    });
}

function resize() {
    const w = getWidth();
    const h = getHeight();
    if (w > 0 && h > 0) {
        skinViewer.width = w;
        skinViewer.height = h;
    }
}

window.addEventListener('resize', resize);
setTimeout(resize, 100);
setTimeout(resize, 500);

function loadSkin(skinUrl, model = "auto-detect") {
    skinViewer.loadSkin(skinUrl, { model: model });
}

function loadSkinFromData(base64Data, model = "auto-detect") {
    // base64Data should be a data URL: "data:image/png;base64,..."
    skinViewer.loadSkin(base64Data, { model: model });
}

function loadCape(capeUrl) {
    skinViewer.loadCape(capeUrl);
}