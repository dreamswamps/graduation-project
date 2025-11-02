<template>
  <div class="Home_Title">Home Page</div>
  <div class="Data_Block">
    <div>
      UserID:<input type="text" v-model="data.user_id">
      <br>
      PassWord:<input type="password" v-model="data.pwd">
    </div>
    <button v-on:click="ClearData">Clear</button>
    <div>
      UserID:{{ data.user_id }}
      <br>
      PassWord:{{ data.pwd }}
    </div>
  </div>
  <div class="Card_Box">
    <span style="color: red;" v-if="!data.pwd">
      The Password is empty.
    </span>
    <span style="color: red;" v-else-if="data.pwd && /^.{0,7}$/.test(data.pwd)">
      Your Password is too short.Please use more than 8-digit
    </span>
    <span style="color: green;" v-else-if="data.pwd">
      The Password is Ok.
      Your Password strength got {{ PWDStrengthCheck() }} .
    </span>
    <div style="display: flex;">
      <div class="Card" v-for="item in data.card">
        {{ item }}
      </div>
    </div>
    <select style="width: 200px;height: 36px;font-size: 24px;">
      <option v-for="item in data.option">{{ item }}</option>
    </select>
  </div>
  <RouterLink to="/">Go to Index Page.</RouterLink>
  <RouterLink to="/manager/info">Go to UserInfo Page.</RouterLink>

  <div>
    Path value:
    {{ id }},{{ pwd }}
  </div>
</template>

<script setup>
import router from '@/router';
import { reactive, ref } from 'vue';

const CreateDataForm = ()=>({
  user_id:"",
  pwd:null,
  card:["卡片1","QWQ","OAO","OWO"],
  option:["Full Clothes","Shirtless","Only Underwaer","Nude","Cum Body"]
})

const data = reactive(CreateDataForm());

const ClearData=()=>{
  Object.assign(data, CreateDataForm());
}
const PWDStrengthCheck=()=>{
  if (!data.pwd || data.pwd.length < 8) return 0;

  const regular_form = {
    lower: /[a-z]/.test(data.pwd) ,
    upper: /[A-Z]/.test(data.pwd) ,
    number: /[0-9]/.test(data.pwd) ,
    special: /[^a-zA-Z0-9]/.test(data.pwd) ,
  }
  // 返回真值的数量
  return Object.values(regular_form).filter(Boolean);
}
const id = router.currentRoute.value.query.id;
const pwd = router.currentRoute.value.query.pwd;

</script>

<style scoped>
.Home_Title{
  font-size: 24px;
  text-align: center;
  font-weight: 600;
  color: blue;
  text-decoration: underline;
}
.Data_Block{
  margin: 20px;
  padding: 10px;
  border-radius: 15px;
  border: 2px solid rgba(255, 0, 0, 0.5);
  box-shadow: 2px 2px 2px rgba(255, 0, 0, 0.2);
}
.Card_Box{
  margin: 20px;
  padding: 10px;
  border: 2px solid red;
}
.Card{
  margin-top: 10px;
  margin-right: 20px;
  width: 200px;
  height: 150px;
  background-color: rgba(255, 255, 0, 0.2);
  border: 2px solid yellow;
  border-radius: 15px;
  font-size: 32px;
  color: red;
  text-align: center;
  line-height: 146px;
}
</style>
