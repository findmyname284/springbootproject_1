class API {
    static async get(url) {
        const response = await fetch(url);
        return response.json();
    }

    static async post(url, data) {
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        return response.json();
    }

    
    static async put(url, data) {
        const response = await fetch(url, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        return response.json();
    }

    static async delete(url) {
        const response = await fetch(url, {
            method: 'DELETE',
        });
        return response.json();
    }
    
    static async request(url, options) {
        const response = await fetch(url, options);
        return response.json();
    }
}