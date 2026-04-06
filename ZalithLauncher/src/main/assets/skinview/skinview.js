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

// ------------------- 新增：双击回正视角功能 -------------------

// 1. 保存初始的摄像机位置和控制器目标点（克隆以避免被修改）
const defaultCameraPos = skinViewer.camera.position.clone();
const defaultControlsTarget = skinViewer.controls.target.clone();
let resetAnimationId = null;

// 2. 监听容器的双击事件
container.addEventListener("dblclick", () => {
    // 如果已经在回正动画中，先取消之前的动画
    if (resetAnimationId) {
        cancelAnimationFrame(resetAnimationId);
    }

    const animateReset = () => {
        // --- 核心优化：分离 Target 插值与摄像机轨道插值 ---

        // 1. Target（聚焦点）依然可以使用直线插值，因为它通常在中心
        skinViewer.controls.target.lerp(defaultControlsTarget, 0.1);

        // 2. 计算当前摄像机相对于 Target 的偏移量
        const currentOffset = skinViewer.camera.position.clone().sub(skinViewer.controls.target);
        const defaultOffset = defaultCameraPos.clone().sub(defaultControlsTarget);

        // 3. 分别计算当前距离和目标距离（轨道半径）
        const currentRadius = currentOffset.length();
        const defaultRadius = defaultOffset.length();
        
        // 使用简单的缓动公式计算下一帧的半径
        const nextRadius = currentRadius + (defaultRadius - currentRadius) * 0.1;

        // 4. 分离出方向向量并进行插值，然后重新归一化（模拟球面插值）
        const currentDir = currentOffset.normalize();
        const defaultDir = defaultOffset.normalize();
        const nextDir = currentDir.lerp(defaultDir, 0.1).normalize();

        // 5. 用新的 Target 位置 + 新的方向 * 新的半径，得出最终的摄像机位置
        const nextPos = skinViewer.controls.target.clone().add(nextDir.multiplyScalar(nextRadius));
        skinViewer.camera.position.copy(nextPos);

        skinViewer.controls.update();

        // ------------------------------------------------

        // 计算当前位置与目标位置的距离，用于判断动画是否结束
        const distPos = skinViewer.camera.position.distanceTo(defaultCameraPos);
        const distTarget = skinViewer.controls.target.distanceTo(defaultControlsTarget);

        // 判断阈值缩小到 0.05，让动画结尾更顺滑
        if (distPos > 0.05 || distTarget > 0.05) {
            resetAnimationId = requestAnimationFrame(animateReset);
        } else {
            // 彻底贴合
            skinViewer.camera.position.copy(defaultCameraPos);
            skinViewer.controls.target.copy(defaultControlsTarget);
            skinViewer.controls.update();
            resetAnimationId = null;
        }
    };

    // 启动动画
    animateReset();
});

// 可选：如果用户在回正动画播放时主动拖拽了模型，打断回正动画
if (skinViewer.controls) {
    skinViewer.controls.addEventListener("start", () => {
        if (resetAnimationId) {
            cancelAnimationFrame(resetAnimationId);
            resetAnimationId = null;
        }
    });
}
// -----------------------------------------------------------

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