function getCsrf() {
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    const headerMeta = document.querySelector('meta[name="_csrf_header"]');

    if (!tokenMeta || !headerMeta) {
        console.warn("CSRF meta tags no encontradas");
        return null;
    }

    return {
        token: tokenMeta.content,
        header: headerMeta.content
    };
}

async function secureFetch(url, options = {}) {
    const csrf = getCsrf();

    const headers = new Headers(options.headers || {});

    if (csrf) {
        headers.set(csrf.header, csrf.token);
    }

    return fetch(url, {
        ...options,
        headers,
        credentials: 'same-origin'

    });
}
