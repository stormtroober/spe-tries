<script setup>
import axios from 'axios';
import MinimalOptionApiTable from './minimal-options-api/Table.vue';
import MinimalCompositionApiTable from './minimal-composition-api/Table.vue';
import AccessibleOptionsApiTable from './accessible-options-api/Table.vue';
import AccessibleCompositionApiTable from './accessible-composition-api/Table.vue';
</script>

<template>
    <MinimalOptionApiTable />
    <MinimalCompositionApiTable />
    <AccessibleOptionsApiTable />
    <AccessibleCompositionApiTable />
</template>
