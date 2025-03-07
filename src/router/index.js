import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../../vue_demo1/src/views/LoginView.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView
  }
  // ... other routes
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router 