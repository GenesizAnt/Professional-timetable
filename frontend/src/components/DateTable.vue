<template>
  <table>
    <tr v-for="row in tableData" :key="row.id">
      <td>{{row.time}}</td>
      <td>{{row.name}}</td>
      <td>{{getStatus(row.status)}}</td>
    </tr>
  </table>
  <button @click="getTableData">refresh</button>
</template>

<script setup>
import {onMounted, ref} from "vue";

defineProps({
  msg: {
    type: String,
    required: true
  }
})

const tableData = ref([])

const getStatus = (status)=>{
  switch (status) {
    case 0:
      return 'Свободно'
    case 1:
      return 'Не свободно'
    case 2:
      return 'Ошибка'
  }
}

const getTableData = async ()=>{

  const data = await fetch('http://localhost:8080/visitors/testtt')
  tableData.value = data
  //     [
  //   {id:1, time: '12:00', name: "Иванов Иван", status:1},
  //   {id:2, time: '13:00', name: "Иванов Иван", status:0},
  //   {id:3, time: '14:00', name: "Иванов Иван", status:2}
  // ]
}

onMounted(()=>{
  getTableData()
})
</script>

<style scoped>
h1 {
  font-weight: 500;
  font-size: 2.6rem;
  position: relative;
  top: -10px;
}

h3 {
  font-size: 1.2rem;
}

.greetings h1,
.greetings h3 {
  text-align: center;
}

@media (min-width: 1024px) {
  .greetings h1,
  .greetings h3 {
    text-align: left;
  }
}
</style>
