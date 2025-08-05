const API_BASE = "/api/user";

/* LOGIN HANDLER */
const loginForm = document.getElementById("loginForm");
if (loginForm) {
  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const username = document.getElementById("loginUsername").value.trim();
    const password = document.getElementById("loginPassword").value.trim();

    const res = await fetch(`${API_BASE}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password })
    });

    const data = await res.json();
    if (res.ok) {
      localStorage.setItem("username", data.username);
      localStorage.setItem("userRole", data.role.toLowerCase());
      window.location.href = "main.html"; // redirect to main after login
    } else {
      document.getElementById("loginMsg").innerText = data.message || "登录失败";
    }
  });
}

/* SIGN-UP HANDLER */
const signupForm = document.getElementById("signupForm");
if (signupForm) {
  signupForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const username = document.getElementById("suUsername").value.trim();
    const email = document.getElementById("suEmail").value.trim();
    const password = document.getElementById("suPassword").value.trim();
    const confirmPassword = document.getElementById("suConfirmPassword").value.trim();

    if (password !== confirmPassword) {
      document.getElementById("signupError").innerText = "两次密码不一致";
      return;
    }

    const res = await fetch(`${API_BASE}/signup`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, email, password })
    });

    const text = await res.text();
    if (res.ok) {
      alert("注册成功，请登录！");
      window.location.href = "index.html"; // back to login
    } else {
      document.getElementById("signupMsg").innerText = text;
    }
  });
}

/* RECOVER HANDLER */
const recoverForm = document.getElementById("recoverForm");
if (recoverForm) {
  recoverForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const email = document.getElementById("recoverEmail").value.trim();

    const res = await fetch(`${API_BASE}/reset-password/request`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email })
    });

    const text = await res.text();
    document.getElementById("recoverMsg").innerText = text;
  });
}

/* RESET PASSWORD HANDLER */
const resetForm = document.getElementById("resetForm");
if (resetForm) {
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get("token");

  resetForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const newPassword = document.getElementById("newPassword").value.trim();

    const res = await fetch(`${API_BASE}/reset-password/confirm`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ token, newPassword })
    });

    const msg = await res.text();
    document.getElementById("resetMsg").innerText = msg;

    if (res.ok) {
      alert("密码已重置，请重新登录！");
      window.location.href = "index.html"; // back to login
    }
  });
}

/* ACCOUNT PAGE: CHANGE PASSWORD + LOGOUT */
const pwChangeForm = document.getElementById("pwChangeForm");
if (pwChangeForm) {
  pwChangeForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const username = localStorage.getItem("username");
    const oldPassword = document.getElementById("oldPassword").value.trim();
    const newPassword = document.getElementById("newPassword").value.trim();

    const res = await fetch(`${API_BASE}/change-password`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, oldPassword, newPassword })
    });

    const msg = await res.text();
    document.getElementById("accountMsg").innerText = msg;

    if (res.ok) {
      alert("密码修改成功");
      pwChangeForm.reset();
    }
  });
}

const logoutBtn = document.getElementById("logoutBtn");
if (logoutBtn) {
  logoutBtn.addEventListener("click", () => {
    localStorage.clear();
    window.location.href = "index.html"; // back to login
  });
}

/* Load account info */
window.addEventListener("DOMContentLoaded", async () => {
  if (window.location.pathname.includes("account.html")) {
    const username = localStorage.getItem("username");
    if (!username) {
      window.location.href = "index.html";
      return;
    }

    const res = await fetch(`${API_BASE}/get/${username}`);
    if (res.ok) {
      const data = await res.json();
      document.getElementById("accountUsername").innerText = data.username;
      document.getElementById("accountEmail").innerText = data.email;
      document.getElementById("accountRole").innerText = data.role;
    } else {
      alert("无法加载账户信息");
    }
  }
});

