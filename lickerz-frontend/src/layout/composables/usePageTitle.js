import { watch } from 'vue';
import { useRoute } from 'vue-router';

export function usePageTitle(titleMap) {
    const route = useRoute();

    watch(
        () => route.path,
        (path) => {
            const title = titleMap[path] || 'Lickerz';
            document.title = title;
        },
        { immediate: true }
    );
}