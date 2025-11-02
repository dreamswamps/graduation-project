<template>
    <el-text>OAO</el-text>
    <el-button color="#00ff00" size="large" icon="Pointer"
    @click="ButtonClick" :style="color_data">
        Click
    </el-button>
    <div class="ViewNum_Box">
        <el-icon :size="24" color="rgba(128, 128, 128, 0.8)"><View /></el-icon>
        {{ data.view }}
    </div>
    <div class="SearchInput_Box">
        <el-input
            v-model="input"
            style="width: 240px"
            placeholder="Type something..."
            prefix-icon="Search"
            clearable
        />
        <br>
        <el-input
            v-model="textarea"
            style="width: 240px"
            :rows="2"
            type="textarea"
            :autosize="{minRows: 2 ,maxRows: 4}"
            placeholder="Please input"/>
    </div>
    <div class="Select_Box">
        <el-select v-model="select_single_option.select_data" placeholder="Please select..." 
            clearable size="large" style="width: 240px;">
            <el-option
                v-for="item in select_single_option.select_option"
                :key="item.key"
                :label="item.key"
                :value="item.value"
                :disabled="item.disabled"
            />
        </el-select>
        Your select the option's value is {{ select_single_option.select_data }}
    </div>
    <div class="Select_Box">
        <el-select v-model="select_multiple_option.values" placeholder="Make your choices..."
            size="large" style="width: 240px;" 
            multiple collapse-tags collapse-tags-tooltip>
            <el-option
                v-for="item in select_multiple_option.options"
                :key="item.key"
                :label="item.key"
                :value="item.value"    
            />
        </el-select>
        <br>
        <el-text style="border: 1px solid red; background-color: rgba(0, 255, 255, 0.2); margin: 6px;"
        v-for="value in select_multiple_option.values">{{ value }}</el-text>
    </div>
    <div class="mb-2 ml-4" style="margin: 30px; gap: 8px;">
        <el-radio-group :disabled="radio_disabled === 'false'" v-model="radio_gender">
            <el-radio-button value="man" size="large">♂</el-radio-button>
            <el-radio-button value="woman" size="large">♀</el-radio-button>
        </el-radio-group>
        {{ radio_gender }}
        <el-radio-group v-model="radio_disabled">
            <el-radio value="true" size="large">可用</el-radio>
            <el-radio value="false" size="large">禁用</el-radio>
        </el-radio-group>
        {{ radio_disabled }}
    </div>
    <div style="margin: 30px; gap: 8px;">
        <el-checkbox-group v-model="check_list">
            <el-checkbox v-for="item in select_multiple_option.options"  :label="item.key" :value="item.value" border="true"/>
        </el-checkbox-group>
        {{ check_list }}
    </div>
    <!-- 读取的位置是public文件夹 -->
    <div>
        <el-image :src="url"/>
    </div>
    <div>
        <div>
            <el-radio-group v-model="data_picker_size">
                <el-radio value="large">Large</el-radio>
                <el-radio value="default">Default</el-radio>
                <el-radio value="small">Small</el-radio>
        </el-radio-group>
        </div>
        <el-date-picker
            v-model="day_value"
            type="daterange"
            placeholder="Pick a day..."
            :size="data_picker_size"
            format="YYYY/MM/DD"
            value-format="YYYY-MM-DD"
            range-separator="-"
            start-placeholder="Start Date"
            end-placeholder="End Date"
        />
        <el-date-picker
            v-model="time_value"
            type="datetime"
            placeholder="Pick a day and time"
            :size="data_picker_size"
            format="YYYY/MM/DD hh:mm:ss"
            value-format="YYYY-MM-DD h:m:s a"
        />
        <span>
            {{ day_value }}
            {{ time_value }}
        </span>
    </div>
    <div>
        <el-table :data="select_multiple_option.options" border="true" stripe height="250" style="width: 100%">
            <el-table-column prop="key" label="Select" width="180" />
            <el-table-column prop="value" label="Name" width="180" />
            <el-table-column label="Detail">
                <template #default="scope">
                    <div v-html="scope.row.detail"></div>
                </template>
            </el-table-column>
            <el-table-column label="Operate">
                <template #default="scope">
                    <el-button type="primary" @click="EditDetail(scope.row)">Edit Details</el-button>
                </template>
            </el-table-column>
        </el-table>
    </div class='Box'>
        <RouterLink to="/home">Go to Home</RouterLink>
        <!-- 编程式路由 -->
        <el-button type="primary" @click="router.push('/home')">push.跳转</el-button>
        <el-button type="primary" @click="router.replace('/home')">replace.跳转</el-button>
    <div>
    <div class="Box">
        <!-- 路径传参 -->
        <el-button type="primary" @click="router.push({
            path:'/home',
            query:{
                id:1,
                pwd:'123456'
            }
        })">路径传参跳转</el-button>
    </div>

    </div>

    <el-dialog v-model="dialog.dialog_visible" title="富文本编辑器" width="800">
        <div style="border: 1px solid #ccc;padding: 20px;">
            <Toolbar
                style="border-bottom: 1px solid #ccc"
                :editor="editorRef"
                :mode="mode"
            />
            <Editor
                style="height: 500px; overflow-y: hidden;"
                v-model="dialog.form.detail"
                :defaultConfig="editorConfig"
                :mode="mode"
                @onCreated="handleCreated"
            />
        </div>
        <template #footer>
        <div class="dialog-footer">
            <el-button type="danger" @click="dialog.dialog_visible = false">取消</el-button>
            <el-button type="success" @click="SaveContent">确认</el-button>
        </div>
        </template>
    </el-dialog>
    
</template>
<script setup>
import router from '@/router';
import { reactive, ref } from 'vue'
// 如果使用全局导入，则可以直接使用icon，如果使用:icon则需要额外导入
// import { Pointer } from '@element-plus/icons-vue'

    const CreateColorDataForm=()=>({
        color:"red"
    });
    const CreateDataForm=()=>({
        view:100
    });
    const color_data = reactive(CreateColorDataForm());
    const data = reactive(CreateDataForm());
    const ButtonClick=()=>{
        alert("The Button is clicked.The color has changed.");
        color_data.color = "white";
    };
    const input = ref();
    const textarea = ref();
    const select_single_option = reactive({
        select_data : "",
        select_option : [
            { 
                key:"OP1",
                value:1,
            },
            {
                key:"OP2",
                value:2,
                disabled: true,
            },
            {
                key:"OP3",
                value:3,
            },
        ]
    });
    const select_multiple_option = reactive({
        values:[],
        options:[
            {
                key:"Attack",
                value:"damage 10 to enemy.",
                detail:'<h1>OAO<h1/>'
            },
            {
                key:"Defence",
                value:"reduce 50% damage got.",
                detail:'<h1>OWO<h1/>'
            },
            {
                key:"Skill",
                value:"cost 20 MP to deal 25 damage.",
                detail:'<h1>XMX<h1/>'
            },
            {
                key:"Heal",
                value:"restore 20% lost HP.",
                detail:'<h1>OVO<h1/>'
            },
            {
                key:"Escape",
                value:"Try to escape fight with 50% chance.",
                detail:'<h1>OAO<h1/>'
            },
        ],
    })
    const radio_gender = ref()
    const radio_disabled = ref(false)
    const check_list = ref([])
    const url = "/favicon.ico"
    const time_value = ref("")
    const day_value = ref("")
    const data_picker_size = ref("default")


import '@wangeditor/editor/dist/css/style.css' // 引入 css

import { onBeforeUnmount, shallowRef, onMounted } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const dialog = reactive({
    dialog_visible:false,
    form:{}
})

// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef()
const mode = 'default'   // 或 'simple'
const editorConfig = { placeholder: '请输入内容...' }

// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
    const editor = editorRef.value
    if (editor == null) return
    editor.destroy()
})

const handleCreated = (editor) => {
    editorRef.value = editor // 记录 editor 实例，重要！
}

const EditDetail=(row)=>{
    dialog.dialog_visible = true;
    dialog.form = JSON.parse(JSON.stringify(row));
}

</script>
<style scoped>
.ViewNum_Box{
    margin: 30px; 
    color:rgba(128, 128, 128, 0.8);
    display: flex; 
    align-items: center; 
    gap: 8px;
}
.SearchInput_Box{
    margin: 30px;
    gap: 8px;
}
.Select_Box{
    margin: 30px;
    gap: 8px;
}
.Box{
    margin: 30px;
}
</style>