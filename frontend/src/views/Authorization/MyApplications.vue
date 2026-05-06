<template>
    <div class="MyApplications">
        <div class="page-header">
            <h2 class="page-title">我的申请</h2>
        </div>

        <ApprovalTableTemplate
            :table-data="data.tableData"
            :loading="isLoading"
            :fields-white-list="data.tableWhiteList"
            :translation-object="translationObject"
            :translation-field-list="translationFieldList"
            :show-detail-btn="true"
            :show-func-btn="false"
            :divider-text="'我的申请'"
            @detail-click="handleDetail" />

        <div class="pagination-box">
            <el-pagination
                @size-change="Load()"
                @current-change="Load()"
                layout="total, sizes, prev, pager, next, jumper"
                background
                v-model:current-page="data.pageNum"
                v-model:page-size="data.pageSize"
                :page-sizes="[5, 10, 15, 20]"
                v-model:total="data.total" />
        </div>
    </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import ApprovalTableTemplate from './Components/ApprovalTableTemplate.vue';
import { optionTypeMap, approvalStatusTextMap } from './RelationMap';
import request from '@/utils/request';
import { ElMessage } from 'element-plus';

const router = useRouter();

const data = reactive({
    pageNum: 1,
    pageSize: 10,
    total: 0,
    tableData: [],
    tableWhiteList: ['aid', 'type', 'apply_time', 'approval'],
});

const isLoading = ref(true);
const translationObject = ref({...optionTypeMap, ...approvalStatusTextMap});
const translationFieldList = ref(['type', 'approval']);
let userID = null;

const Load = () => {
    isLoading.value = true;
    request.get('/application/check', {
        params: { apply_account: userID , isID: true },
    }).then(res => {
        if (res.code === '200') {
            data.tableData = res.data.obj ?? res.data;
            data.total = res.data.total ?? data.tableData.length;
            data.pageNum = res.data.pageNum ?? 1;
            data.pageSize = res.data.pageSize ?? 10;
        } else {
            ElMessage.error(res.msg);
        }
    }).finally(() => {
        setTimeout(() => { isLoading.value = false; }, 500);
    });
};

const handleDetail = (rowData) => {
    sessionStorage.setItem('approvalDetail', JSON.stringify(rowData));
    router.push({ name: '申请详情' });
};

onMounted(() => {
    userID = JSON.parse(window.localStorage.getItem('current_user')).id;
    Load();
});
</script>

<style scoped>
.MyApplications {
    padding: 24px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf4 100%);
    min-height: 100vh;
    animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}

.page-header {
    margin-bottom: 28px;
    text-align: center;
}

.page-title {
    font-size: 32px;
    font-weight: 600;
    margin: 0 0 8px 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.page-subtitle {
    font-size: 14px;
    color: #7f8c8d;
    margin: 0;
}

.pagination-box {
    display: flex;
    justify-content: flex-end;
    margin-top: 8px;
    padding: 0 4px;
}

.el-button {
    border-radius: 8px;
    font-weight: 500;
    letter-spacing: 1px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-button:hover { transform: translateY(-2px); }
.el-button:active { transform: translateY(0); }

:deep(.el-divider__text) {
    background-color: transparent !important;
}
</style>
