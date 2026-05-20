<template>
  <footer class="bg-dark text-white py-5">
    <div class="container">
      <div class="row">
        <div class="col-md-4 mb-4">
          <div class="d-flex align-items-center mb-3">
            <i class="fas fa-home text-primary me-2" style="font-size: 1.5rem;"></i>
            <h4 class="fw-bold mb-0">寻亲网站</h4>
          </div>
          <p class="text-secondary">让爱回家，让亲情重聚</p>
          <div class="d-flex gap-3 mt-3">
            <a href="#" class="text-secondary hover-primary" style="font-size: 1.25rem;">
              <i class="fab fa-weixin"></i>
            </a>
            <a href="#" class="text-secondary hover-primary" style="font-size: 1.25rem;">
              <i class="fab fa-weibo"></i>
            </a>
            <a href="#" class="text-secondary hover-primary" style="font-size: 1.25rem;">
              <i class="fab fa-qq"></i>
            </a>
          </div>
        </div>
        <div class="col-md-4 mb-4">
          <h4 class="fw-bold mb-4">快速链接</h4>
          <ul class="list-unstyled">
            <li class="mb-2"><router-link to="/" class="text-secondary text-decoration-none">首页</router-link></li>
            <li class="mb-2"><router-link to="/success-cases" class="text-secondary text-decoration-none">成功案例</router-link></li>
            <li class="mb-2"><router-link to="/community" class="text-secondary text-decoration-none">社群交流</router-link></li>
            <li class="mb-2"><router-link to="/volunteer" class="text-secondary text-decoration-none">志愿者招募</router-link></li>
          </ul>
        </div>
        <div class="col-md-4">
          <h4 class="fw-bold mb-4">联系我们</h4>
          <ul class="list-unstyled">
            <li class="mb-3 d-flex align-items-center">
              <i class="fas fa-phone text-primary me-3"></i>
              <span class="text-secondary">400-123-4567</span>
            </li>
            <li class="mb-3 d-flex align-items-center">
              <i class="fas fa-envelope text-primary me-3"></i>
              <span class="text-secondary">contact@xunqin.com</span>
            </li>
            <li class="mb-3 d-flex align-items-start">
              <i class="fas fa-map-marker-alt text-primary me-3 mt-1"></i>
              <span class="text-secondary">北京市朝阳区建国路88号</span>
            </li>
          </ul>
        </div>
      </div>
      <div class="border-top border-secondary pt-4 mt-4 text-center">
        <p class="text-secondary mb-2">&copy; 2026 寻亲网站 版权所有</p>
        <div>
          <a href="#" class="text-secondary text-decoration-none me-4">隐私政策</a>
          <a href="#" class="text-secondary text-decoration-none me-4">使用条款</a>
          <a href="#" class="text-secondary text-decoration-none">联系我们</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
</script>

<style scoped>
.hover-primary:hover {
  color: var(--primary-color) !important;
  transition: color 0.3s ease;
}
</style>