<template>
  <div>
    <h2 class="mb-4">我的寻亲信息</h2>
    <div class="table-responsive">
      <table class="table table-hover">
        <thead class="table-light">
          <tr><th>ID</th><th>姓名</th><th>性别</th><th>失踪日期</th><th>状态</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-if="loading"><td colspan="6" class="text-center py-4"><div class="loader"></div></td></tr>
          <tr v-else-if="list.length === 0"><td colspan="6" class="text-center text-muted py-4">暂无寻亲信息</td></tr>
          <tr v-for="item in list" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.gender || '-' }}</td>
            <td>{{ item.missingDate || '-' }}</td>
            <td><span class="status-badge" :class="'status-' + statusClass(item.status)">{{ statusText(item.status) }}</span></td>
            <td>
              <button class="btn btn-sm btn-outline-info me-1" @click="viewDetail(item)"><i class="fas fa-eye"></i></button>
              <button class="btn btn-sm btn-outline-danger" @click="deleteItem(item)"><i class="fas fa-trash"></i></button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-if="showDetail" class="modal d-block" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">寻亲详情 - {{ detailItem.name }}</h5>
            <button type="button" class="btn-close" @click="showDetail = false"></button>
          </div>
          <div class="modal-body">
            <div class="row">
              <div class="col-md-6"><strong>姓名：</strong>{{ detailItem.name }}</div>
              <div class="col-md-6"><strong>性别：</strong>{{ detailItem.gender || '-' }}</div>
              <div class="col-md-6"><strong>失踪年龄：</strong>{{ detailItem.ageAtMissing || '-' }}</div>
              <div class="col-md-6"><strong>身高：</strong>{{ detailItem.height || '-' }}cm</div>
              <div class="col-md-6"><strong>失踪日期：</strong>{{ detailItem.missingDate || '-' }}</div>
              <div class="col-md-6"><strong>失踪地点：</strong>{{ detailItem.missingLocation || '-' }}</div>
              <div class="col-12 mt-2"><strong>体貌特征：</strong>{{ detailItem.appearance || '-' }}</div>
              <div class="col-12"><strong>详细描述：</strong>{{ detailItem.description || '-' }}</div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showDetail = false">关闭</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { missingPersonApi } from '@/api'

const list = ref([])
const loading = ref(true)
const showDetail = ref(false)
const detailItem = ref({})

function statusClass(s) {
  if (s === 1) return 'approved'
  if (s === 2) return 'rejected'
  return 'pending'
}

function statusText(s) {
  if (s === 1) return '已通过'
  if (s === 2) return '已拒绝'
  return '审核中'
}

async function loadData() {
  loading.value = true
  try {
    const res = await missingPersonApi().getMyList()
    if (res.code === 200) list.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
  } catch (e) { console.error('加载失败:', e) }
  finally { loading.value = false }
}

function viewDetail(item) {
  detailItem.value = item
  showDetail.value = true
}

async function deleteItem(item) {
  if (!confirm(`确定删除「${item.name}」的寻亲信息吗？`)) return
  try {
    const res = await missingPersonApi().delete(item.id)
    if (res.code === 200) list.value = list.value.filter(i => i.id !== item.id)
  } catch (e) { alert('删除失败') }
}

onMounted(() => loadData())
</script>