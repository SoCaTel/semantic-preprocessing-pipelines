<template>
  <v-app>
    <app-header />
    <v-content>
      <router-view />
    </v-content>
  </v-app>
</template>

<script>
  import Vue from "vue";
  import Header from "./header";

  export default Vue.extend({
    "name": "app",
    "components": {
      "app-header": Header
    }
  });
</script>