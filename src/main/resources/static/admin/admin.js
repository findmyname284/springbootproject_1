class AdminPanel {
    constructor() {
        this.loadInitialData();
    }

    async loadInitialData() {
        try {
            const [users, roles] = await Promise.all([
                API.get('/api/admin/users'),
                API.get('/api/admin/roles')
            ]);

            this.renderUsers(users);
            this.populateRoleSelect(roles);
        } catch (error) {
            notification.className = 'error';
            showNotification('Ошибка загрузки данных');
        }
    }

    async populateRoleSelect(roles) {
        const selects = document.querySelectorAll('.select-role');
        selects.forEach(select => {
            select.innerHTML = roles.map(role => `<option value="${role}">${role}</option>`).join('');
        })
    }

    renderUsers(users) {
        const container = document.getElementById('users-list');
        container.innerHTML = users.map(user => `
            <div class="user-card" data-user-id="${user.id}">
                <h3>${user.surname} ${user.name}</h3>
                <p>@${user.username}</p>
                <p>${user.role} · ${user.email} · ${user.phone}</p>
                <p>${user.balance} ₸</p>
                <div class="card-actions">
                    <button class="btn-icon" onclick="AdminPanel.openEditForm(${user.id})">✏️</button>
                    <button class="btn-icon danger" onclick="AdminPanel.deleteUser(${user.id})">🗑️</button>
                </div>
            </div>
        `).join('');
    }

    static handleFormSubmit() {
        const formData = new FormData(event.target);
        const userId = formData.get('id') || '';
        if (formData.get('confirmPassword') != null && formData.get('confirmPassword') !== formData.get('password')) {
            notification.className = 'error';
            showNotification('Пароли не совпадают');
            return;
        }
        console.log(userId)

        try {
            const method = userId ? 'PUT' : 'POST';
            const url = userId ? `/api/admin/users/${userId}` : '/api/admin/users';

            API.request(url, {
                method,
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(Object.fromEntries(formData))
            }).then(data => {
                if (data.error) {
                    notification.className = 'error';
                    showNotification(data.error);
                    return;
                }
                notification.className = 'success';
                showNotification('Данные успешно сохранены');
                Modal.close("user-modal");
                window.location.reload();
            }).catch(error => {
                notification.className = 'error';
                showNotification('Ошибка сохранения данных');
            });
        } catch (error) {
            notification.className = 'error';
            showNotification('Ошибка сохранения данных');
        }
    }

    static async deleteUser(userId) {
        try {
            await API.delete(`/api/admin/users/${userId}`)
            .then(data => {
                if (data.error) {
                    notification.className = 'error';
                    showNotification(data.error);
                    return;
                } else if (data.success) {
                    notification.className = 'success';
                    showNotification(data.success);
                    window.location.reload();
                }
            });
        } catch (error) {
            notification.className = 'error';
            showNotification('Ошибка удаления пользователя');
        }
    }

    static async openEditForm(userId) {
        try {
            Modal.open('user-edit-modal');
            const user = await API.get(`/api/admin/users/${userId}`);

            const form = document.getElementById('user-edit-form');
            form.querySelector('input[name="id"]').value = user.id || '';
            form.querySelector('input[name="name"]').value = user.name || '';
            form.querySelector('input[name="surname"]').value = user.surname || '';
            form.querySelector('input[name="username"]').value = user.username || '';
            form.querySelector('input[name="email"]').value = user.email || '';
            form.querySelector('input[name="phone"]').value = user.phone || '';
            form.querySelector('input[name="balance"]').value = user.balance !== undefined ? user.balance.toFixed(2) : '';
            form.querySelector('input[name="address"]').value = user.address || '';
            form.querySelector('select[name="role"]').value = user.role || '';
        } catch (error) {
            notification.className = 'error';
            showNotification('Ошибка загрузки пользователя');
        }
    }

    
}