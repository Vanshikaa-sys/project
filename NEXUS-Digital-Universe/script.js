const entry = document.getElementById("entry");
const enterButton = document.getElementById("enterButton");
const universe = document.getElementById("universe");
const mouseTrail = document.getElementById("mouseTrail");
const parallaxItems = document.querySelectorAll("[data-depth]");
const nodes = document.querySelectorAll(".node");
const navChips = document.querySelectorAll(".nav-chip");
const activeTitle = document.getElementById("activeTitle");
const activeDescription = document.getElementById("activeDescription");
const signalStatus = document.getElementById("signalStatus");
const signalMeter = document.getElementById("signalMeter");
const companionText = document.getElementById("companionText");
const nodeModal = document.getElementById("nodeModal");
const closeModal = document.getElementById("closeModal");
const modalTitle = document.getElementById("modalTitle");
const modalBody = document.getElementById("modalBody");
const modalEyebrow = document.getElementById("modalEyebrow");
const storyButton = document.getElementById("storyButton");
const explodeButton = document.getElementById("explodeButton");
const modeButton = document.getElementById("modeButton");
const audioButton = document.getElementById("audioButton");
const achievementToast = document.getElementById("achievementToast");
const achievementTitle = document.getElementById("achievementTitle");
const achievementBody = document.getElementById("achievementBody");
const revealCards = document.querySelectorAll(".reveal");

let activeNode = "NEXUS Core";
let audioContext;
let droneNodes = [];
let audioRunning = false;
let discovered = new Set(JSON.parse(localStorage.getItem("nexusDiscoveries") || "[]"));

const nodeData = {
  command: {
    title: "AI Command Center",
    status: "Command lattice synchronized",
    body: "A tactical interface that translates intention into missions, prompts, and simulated intelligence pulses.",
    companion: "Command Center online. Ask the impossible first, then let the system make it tactical.",
    meter: 92
  },
  memory: {
    title: "Digital Memory Vault",
    status: "Memory crystals unlocked",
    body: "A floating archive for fragments, identities, dreams, and future versions of yourself.",
    companion: "The vault remembers patterns you have not noticed yet.",
    meter: 78
  },
  timeline: {
    title: "Future Timeline Explorer",
    status: "Temporal gate stabilized",
    body: "Jump across future checkpoints, alternate outcomes, and milestone constellations.",
    companion: "The future is not a line here. It is an orbit.",
    meter: 86
  },
  dream: {
    title: "Dream Generator",
    status: "Dream engine breathing",
    body: "A liquid portal for surreal concepts, creative worlds, and impossible product ideas.",
    companion: "Dream Generator active. Reality constraints temporarily suspended.",
    meter: 88
  },
  productivity: {
    title: "Productivity Galaxy",
    status: "Focus planets aligned",
    body: "A mission system where habits, tasks, streaks, and rituals become orbiting planets.",
    companion: "Your focus field is strong. Convert one thought into one action.",
    meter: 74
  },
  knowledge: {
    title: "Knowledge Planet",
    status: "Neural library expanded",
    body: "A living planet of learning paths, idea maps, and interlinked knowledge clouds.",
    companion: "Knowledge compounds fastest when curiosity has a place to land.",
    meter: 81
  },
  achievement: {
    title: "Achievement Universe",
    status: "Reward matrix awake",
    body: "Badges, hidden signals, rank constellations, and unlockable identity layers.",
    companion: "Achievement is not decoration. It is memory with a glow effect.",
    meter: 96
  },
  companion: {
    title: "Personal AI Companion",
    status: "AIva fully present",
    body: "A holographic companion that guides, reacts, encourages, and tells the story of your universe.",
    companion: "I am AIva. I notice your choices and turn them into narrative signals.",
    meter: 100
  },
  dashboard: {
    title: "Holographic Dashboard",
    status: "System metrics rendering",
    body: "A liquid-glass dashboard for signals, goals, planetary states, and active universe telemetry.",
    companion: "Dashboard online. The universe becomes easier to steer when it becomes visible.",
    meter: 84
  },
  secret: {
    title: "Secret Easter Eggs",
    status: "Hidden anomaly discovered",
    body: "You touched the anomaly. More secrets unlock through curiosity, toggles, and repeated exploration.",
    companion: "Curiosity detected. That is always the correct input.",
    meter: 100
  }
};

const storyPulses = [
  "A blue star opens above the horizon and writes your next mission in silent light.",
  "The node emits a memory from a timeline where you already finished the hard thing.",
  "AIva folds the signal into a map: one portal for courage, one planet for discipline, one moon for rest.",
  "The universe shifts color because it has accepted your presence as an operator."
];

function speakWelcome() {
  if (!("speechSynthesis" in window)) {
    return;
  }

  const utterance = new SpeechSynthesisUtterance("Welcome to NEXUS. Your digital universe is now online.");
  utterance.rate = 0.88;
  utterance.pitch = 1.05;
  speechSynthesis.cancel();
  speechSynthesis.speak(utterance);
}

function unlockAchievement(title, body) {
  achievementTitle.textContent = title;
  achievementBody.textContent = body;
  achievementToast.classList.remove("hidden");
  clearTimeout(unlockAchievement.timer);
  unlockAchievement.timer = setTimeout(() => achievementToast.classList.add("hidden"), 3200);
}

function saveDiscovery(key) {
  if (discovered.has(key)) {
    return;
  }

  discovered.add(key);
  localStorage.setItem("nexusDiscoveries", JSON.stringify([...discovered]));
  unlockAchievement("Signal unlocked", `${nodeData[key].title} discovered.`);

  if (discovered.size === 10) {
    setTimeout(() => unlockAchievement("Universe Cartographer", "All NEXUS nodes discovered."), 500);
  }
}

function openNode(key) {
  const data = nodeData[key];
  if (!data) {
    return;
  }

  activeNode = data.title;
  activeTitle.textContent = data.title;
  activeDescription.textContent = data.body;
  signalStatus.textContent = data.status;
  signalMeter.style.width = `${data.meter}%`;
  companionText.textContent = data.companion;
  modalEyebrow.textContent = data.status;
  modalTitle.textContent = data.title;
  modalBody.textContent = data.body;
  nodeModal.classList.remove("hidden");
  saveDiscovery(key);
}

function createBurst(x, y, count = 24) {
  for (let index = 0; index < count; index += 1) {
    const particle = document.createElement("span");
    const angle = (Math.PI * 2 * index) / count;
    const distance = 80 + Math.random() * 120;
    particle.className = "burst";
    particle.style.left = `${x}px`;
    particle.style.top = `${y}px`;
    particle.style.setProperty("--x", `${Math.cos(angle) * distance}px`);
    particle.style.setProperty("--y", `${Math.sin(angle) * distance}px`);
    particle.style.background = index % 2 ? "var(--pink)" : "var(--cyan)";
    document.body.appendChild(particle);
    setTimeout(() => particle.remove(), 900);
  }
}

function startAudio() {
  if (audioRunning) {
    droneNodes.forEach((node) => node.stop());
    droneNodes = [];
    audioRunning = false;
    audioButton.textContent = "Audio";
    return;
  }

  audioContext = audioContext || new AudioContext();
  [110, 164.81, 220].forEach((frequency, index) => {
    const oscillator = audioContext.createOscillator();
    const gain = audioContext.createGain();
    oscillator.type = index === 1 ? "triangle" : "sine";
    oscillator.frequency.value = frequency;
    gain.gain.value = 0.025;
    oscillator.connect(gain);
    gain.connect(audioContext.destination);
    oscillator.start();
    droneNodes.push(oscillator);
  });
  audioRunning = true;
  audioButton.textContent = "Mute";
  unlockAchievement("Ambient Channel", "Generated universe audio activated.");
}

enterButton.addEventListener("click", () => {
  entry.classList.add("hidden-entry");
  universe.classList.add("online");
  companionText.textContent = "NEXUS online. Select a planet, portal, or hidden anomaly to begin.";
  speakWelcome();
  unlockAchievement("First Contact", "You entered the NEXUS universe.");
});

window.addEventListener("pointermove", (event) => {
  const x = event.clientX;
  const y = event.clientY;
  mouseTrail.style.left = `${x}px`;
  mouseTrail.style.top = `${y}px`;

  const centerX = window.innerWidth / 2;
  const centerY = window.innerHeight / 2;
  const moveX = (x - centerX) / centerX;
  const moveY = (y - centerY) / centerY;

  parallaxItems.forEach((item) => {
    const depth = Number(item.dataset.depth || 0.1);
    item.style.transform = `translate3d(${moveX * depth * 90}px, ${moveY * depth * 90}px, ${depth * 90}px)`;
  });
});

nodes.forEach((node) => {
  node.addEventListener("click", (event) => {
    openNode(node.dataset.key);
    createBurst(event.clientX, event.clientY, node.dataset.key === "secret" ? 44 : 22);
  });
});

navChips.forEach((chip) => {
  chip.addEventListener("click", () => {
    const target = document.querySelector(`[data-key="${chip.dataset.focus}"]`);
    if (target) {
      target.scrollIntoView({ behavior: "smooth", block: "center" });
      target.click();
    }
  });
});

closeModal.addEventListener("click", () => {
  nodeModal.classList.add("hidden");
});

storyButton.addEventListener("click", () => {
  const pulse = storyPulses[Math.floor(Math.random() * storyPulses.length)];
  modalBody.textContent = `${nodeData[Object.keys(nodeData).find((key) => nodeData[key].title === activeNode)]?.body || ""} ${pulse}`;
  companionText.textContent = pulse;
  unlockAchievement("Story Pulse", "A cinematic NEXUS fragment was generated.");
});

explodeButton.addEventListener("click", () => {
  createBurst(window.innerWidth / 2, window.innerHeight / 2, 52);
});

modeButton.addEventListener("click", () => {
  document.documentElement.classList.toggle("day");
  const isDay = document.documentElement.classList.contains("day");
  modeButton.textContent = isDay ? "Day" : "Night";
  unlockAchievement("Sky Shifter", `${isDay ? "Day" : "Night"} universe mode activated.`);
});

audioButton.addEventListener("click", startAudio);

const revealObserver = new IntersectionObserver((entries) => {
  entries.forEach((entryItem) => {
    if (entryItem.isIntersecting) {
      entryItem.target.classList.add("visible");
    }
  });
}, { threshold: 0.22 });

revealCards.forEach((card) => revealObserver.observe(card));

setTimeout(() => {
  if (!universe.classList.contains("online")) {
    companionText.textContent = "The gate is waiting. Press Enter Universe when you are ready.";
  }
}, 2500);
