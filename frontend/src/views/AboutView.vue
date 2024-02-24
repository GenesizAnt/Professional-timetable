<template>
<DateTable></DateTable>
</template>

<style>
@media (min-width: 1024px) {
  .about {
    min-height: 100vh;
    display: flex;
    align-items: center;
  }
}
</style>
<script>
import DateTable from "@/components/DateTable.vue";
export default {
  components: {DateTable}
}
</script>