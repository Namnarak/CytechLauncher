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

// Center the camera
skinViewer.camera.position.set(0, 10, 50);
if (skinViewer.camera.lookAt) {
    skinViewer.camera.lookAt(0, 16, 0);
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

function resetSkin() {
    skinViewer.loadSkin("steve.png");
    skinViewer.loadCape(null);
}