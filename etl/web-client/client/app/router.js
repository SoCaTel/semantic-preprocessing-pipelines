import Vue from "vue";
import VueRouter from "vue-router";
import {getRegistered} from "./registry"

Vue.use(VueRouter);

const routes = [];
getRegistered().forEach((module) => {
  if (module["path"] && module["name"] && module["component"]) {
    routes.push({
      "path": module["path"],
      "name": module["name"],
      "component": module["component"]
    });
  }
});

const router = new VueRouter({
  "routes": routes
});

export default router;