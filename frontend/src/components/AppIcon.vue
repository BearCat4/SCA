<template>
  <span class="line-icon" :class="{ 'danger-icon': danger }" aria-hidden="true">
    <svg v-if="name === 'home'" viewBox="0 0 24 24"><path d="M4 11l8-7 8 7"/><path d="M6 10v10h12V10"/><path d="M10 20v-5h4v5"/></svg>
    <svg v-else-if="name === 'quick'" viewBox="0 0 24 24"><circle cx="12" cy="12" r="8"/><path d="M12 8v4l3 2"/><path d="M4 4l3 3M20 4l-3 3"/></svg>
    <svg v-else-if="name === 'project'" viewBox="0 0 24 24"><path d="M5 5h14v14H5z"/><path d="M8 9h8M8 13h8M8 17h5"/></svg>
    <svg v-else-if="name === 'assets'" viewBox="0 0 24 24"><path d="M4 17l5-5 4 4 7-8"/><path d="M4 5h16v14H4z"/></svg>
    <svg v-else-if="name === 'chevron-down'" viewBox="0 0 24 24"><path d="M7 10l5 5 5-5"/></svg>
    <svg v-else-if="name === 'chevron-right'" viewBox="0 0 24 24"><path d="M10 7l5 5-5 5"/></svg>
    <svg v-else-if="name === 'folder'" viewBox="0 0 24 24"><path d="M4 5h6l2 3h8v11H4z"/><path d="M7 12h2M7 16h6"/></svg>
    <svg v-else-if="name === 'components'" viewBox="0 0 24 24"><path d="M4 5h16v4H4z"/><path d="M6 9v10M18 9v10M10 13h4M10 17h4"/></svg>
    <svg v-else-if="name === 'risk'" viewBox="0 0 24 24"><circle cx="12" cy="12" r="5"/><path d="M12 3v3M12 18v3M3 12h3M18 12h3M5 5l2.2 2.2M16.8 16.8 19 19M19 5l-2.2 2.2M7.2 16.8 5 19"/></svg>
    <svg v-else-if="name === 'license'" viewBox="0 0 24 24"><path d="M12 3l2.4 4.6L19 10l-4.6 2.4L12 21l-2.4-8.6L5 10l4.6-2.4z"/><path d="M18 3v4M16 5h4"/></svg>
    <svg v-else-if="name === 'warning'" viewBox="0 0 24 24"><path d="M12 3l9 16H3z"/><path d="M12 8v5M12 17h.01"/></svg>
    <svg v-else-if="name === 'failed'" viewBox="0 0 24 24"><circle cx="12" cy="12" r="8"/><path d="M9 9l6 6M15 9l-6 6"/></svg>
    <svg v-else-if="name === 'clock'" viewBox="0 0 24 24"><circle cx="12" cy="12" r="8"/><path d="M12 7v5l3 2"/></svg>
    <svg v-else-if="name === 'severity'" viewBox="0 0 24 24"><path d="M12 2v20M4 6h16M6 18h12"/><path d="M8 10h8v4H8z"/></svg>
    <svg v-else-if="name === 'scan'" viewBox="0 0 24 24"><path d="M5 5h5M14 5h5M5 19h5M14 19h5"/><path d="M5 10V5h5M19 10V5h-5M5 14v5h5M19 14v5h-5"/></svg>
    <svg v-else-if="name === 'policy'" viewBox="0 0 24 24"><path d="M12 3l7 3v5c0 4.5-2.8 7.5-7 10-4.2-2.5-7-5.5-7-10V6z"/><path d="M9 12l2 2 4-5"/></svg>
    <svg v-else-if="name === 'image'" viewBox="0 0 24 24"><path d="M5 7l7-4 7 4-7 4z"/><path d="M5 7v8l7 4 7-4V7"/><path d="M12 11v8"/></svg>
    <svg v-else-if="name === 'report'" viewBox="0 0 24 24"><path d="M6 3h9l3 3v15H6z"/><path d="M14 3v4h4M9 13h6M9 17h4M9 9h2"/></svg>
    <svg v-else-if="name === 'team'" viewBox="0 0 24 24"><circle cx="9" cy="8" r="3"/><circle cx="17" cy="9" r="2"/><path d="M4 19c.8-3 2.4-5 5-5s4.2 2 5 5M14 15c2.5.2 4.2 1.5 5 4"/></svg>
    <svg v-else-if="name === 'settings'" viewBox="0 0 24 24"><circle cx="12" cy="12" r="3"/><path d="M12 3v3M12 18v3M4.2 7.5l2.6 1.5M17.2 15l2.6 1.5M19.8 7.5l-2.6 1.5M6.8 15l-2.6 1.5"/></svg>
    <svg v-else viewBox="0 0 24 24"><path d="M6 5h12v14H6z"/><path d="M9 9h6M9 13h6M9 17h3"/></svg>
  </span>
</template>

<script setup>
defineProps({
  name: { type: String, default: 'empty' },
  danger: { type: Boolean, default: false }
})
</script>
