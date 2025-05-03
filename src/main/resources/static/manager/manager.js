class ManagerPanel {
    constructor() {
        this.loadInitialData();
    }

    async loadInitialData() {
        try {
            const [warehouses, employees] = await Promise.all([
                API.get('/api/manager/warehouses'),
                API.get('/api/manager/employees'),
            ]);

            this.renderWarehouses(warehouses);
            this.renderEmployees(employees);
            this.populateEmployeesSelect(employees);
            this.loadUsersWithoutEmployee();
            this.populateWarehousesSelect(warehouses);
        } catch (error) {
            notification.className = 'error';
            showNotification('Ошибка загрузки данных' + error);
        }
    }

    async populateWarehousesSelect(warehouses) {
        const selects = document.querySelectorAll('.warehouse-select');
        selects.forEach(select => {
            select.innerHTML = warehouses.map(warehouse => `<option value="${warehouse.id}">${warehouse.name}</option>`).join('');
        });
    }

    async populateEmployeesSelect(employees) {
        const select = document.querySelector('.employee-select');
        select.innerHTML = employees.map(employee => `<option value="${employee.id}">${employee.user.surname + ' ' + employee.user.name}</option>`).join('');
    }

    renderWarehouses(warehouses) {
        const container = document.getElementById('warehouses-list');
        if (warehouses.length === 0) {
            container.innerHTML = `<p>Склады не найдены</p>`;
            return;
        }
        container.innerHTML = warehouses.map(warehouse => `
            <div class="user-card" data-warehouse-id="${warehouse.id}">
                <h3>${warehouse.name}</h3>
                <p>${warehouse.address}</p>
                <p>${warehouse.manager ? warehouse.manager?.user.surname + ' ' + warehouse.manager.user.name : "Нет сотрудника"}</p>
                <div class="card-actions">
                    <button class="btn-icon" onclick="ManagerPanel.openEditForm(${warehouse.id})">✏️</button>
                    <button class="btn-icon danger" onclick="ManagerPanel.deleteWarehouse(${warehouse.id})">🗑️</button>
                </div>
            </div>
        `).join('');
    }

    static editWarehouse() {
        const formData = new FormData(event.target);
        const id = formData.get('id');

        try {
            const method = 'PUT';
            const url = '/api/manager/warehouses/' + id;

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
                Modal.close('warehouse-edit-modal');
                notification.className = 'success';
                showNotification('Склад успешно изменен');
                window.location.reload();
            }).catch(error => {
                notification.className = 'error';
                showNotification('Ошибка сохранения данных' + error);
            });
        } catch (error) {
            notification.className = 'error';
            showNotification('Ошибка сохранения данных' + error);
        }
    }

    static async addWarehouse() {
        try {
            const form = document.getElementById('warehouse-form');
            const formData = new FormData(form);

            API.request('/api/manager/warehouses', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(Object.fromEntries(formData))
            })
                .then(data => {
                    if (data.error) {
                        notification.className = 'error';
                        showNotification(data.error);
                        return;
                    }
                    Modal.close('warehouse-modal');
                    notification.className = 'success';
                    showNotification('Склад успешно добавлен');
                    window.location.reload();
                }).catch(error => {
                    notification.className = 'error';
                    showNotification('Ошибка добавления склада');
                });
        } catch (error) {
            notification.className = 'error';
            showNotification('Ошибка добавления склада');
        }
    }

    static async deleteWarehouse(id) {
        try {
            API.request('/api/manager/warehouses', {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ id })
            })
                .then(data => {
                    if (data.error) {
                        notification.className = 'error';
                        showNotification(data.error);
                        return;
                    } else if (data.success) {
                        notification.className = 'success';
                        showNotification('Склад успешно удален');
                        window.location.reload();
                    }
                })
                .catch(error => {
                    notification.className = 'error';
                    showNotification('Ошибка удаления склада' + error);
                });
        } catch (error) {
            notification.className = 'error';
            showNotification('Ошибка удаления склада');
        }
    }

    static async openEditForm(id) {
        try {
            Modal.open('warehouse-edit-modal');
            document.body.style.overflow = 'hidden';
            const warehouse = await API.get(`/api/manager/warehouses/${id}`);

            const form = document.getElementById('warehouse-form-edit');
            form.querySelector('input[name="id"]').value = warehouse.id || '';
            form.querySelector('input[name="name"]').value = warehouse.name || '';
            form.querySelector('input[name="address"]').value = warehouse.address || '';
            const select = form.querySelector('select[name="employee-id"]');
            // populateEmployeesSelect(select);
            // select.selectedOptions[0].value = warehouse.manager?.id || '';
            // select.selectedOptions[0].text = warehouse.manager?.user.surname || '' + ' ' + warehouse.manager?.user.name || '';
        } catch (error) {
            notification.className = 'error';
            showNotification('Ошибка загрузки пользователя type:' + error);
        }
    }

    async loadUsersWithoutEmployee() {
        try {
            const users = await API.get('/api/manager/employees/users');
            this.populateUserSelect(users);
        } catch (error) {
            this.showError('Ошибка загрузки пользователей', error);
        }
    }

    renderEmployees(employees) {
        const container = document.getElementById('employees-list');
        container.innerHTML = employees.map(employee => `
            <div class="user-card" data-employee-id="${employee.id}">
                <h3>${employee.user.surname} ${employee.user.name}</h3>
                <p>💼 ${employee.salary} ₸</p>
                <p>📅 ${new Date(employee.hireDate).toLocaleDateString()}</p>
                <div class="card-actions">
                    <button class="btn-icon" onclick="ManagerPanel.openEmployeeEditForm(${employee.id})">✏️</button>
                    <button class="btn-icon danger" onclick="ManagerPanel.deleteEmployee(${employee.id})">🗑️</button>
                </div>
            </div>
        `).join('');
    }

    static async openEmployeeCreateForm() {
        Modal.open('employee-modal');
    }

    static async openEmployeeEditForm(id) {
        try {
            const employee = await API.get(`/api/manager/employees/${id}`);
            Modal.open('employee-edit-modal');

            const form = document.getElementById('employee-edit-form');
            form.elements['id'].value = employee.id;
            form.elements['salary'].value = employee.salary;
            form.elements['iin'].value = employee.iin;
        } catch (error) {
            this.showError('Ошибка загрузки сотрудника', error);
        }
    }

    static async handleEmployeeCreate(event) {
        event.preventDefault();
        const formData = new FormData(event.target);

        try {
            await API.post('/api/manager/employees', {
                userId: formData.get('userId'),
                salary: formData.get('salary'),
                iin: formData.get('iin')
            }).then(data => {
                if (data.error) {
                    this.showError('Ошибка создания сотрудника', data.error);
                    return;
                }
                Modal.close('employee-modal');
                this.showSuccess('Сотрудник успешно создан');
                window.location.reload();
            })
        } catch (error) {
            this.showError('Ошибка создания сотрудника', error);
        }
    }

    static async handleEmployeeUpdate(event) {
        event.preventDefault();
        const formData = new FormData(event.target);
        const id = formData.get('id');

        try {
            await API.put(`/api/manager/employees/${id}`, {
                phone: formData.get('phone'),
                salary: formData.get('salary'),
                iin: formData.get('iin')
            }).then(data => {
                if (data.error) {
                    this.showError('Ошибка обновления данных', data.error);
                    return;
                }
                Modal.close('employee-edit-modal');
                this.showSuccess('Данные сотрудника обновлены');
                window.location.reload();
            });
        } catch (error) {
            this.showError('Ошибка обновления данных', error);
        }
    }

    static async deleteEmployee(id) {
        if (!confirm('Вы уверены что хотите удалить сотрудника?')) return;

        try {
            await API.delete(`/api/manager/employees/${id}`);
            this.showSuccess('Сотрудник успешно удален');
            window.location.reload();
        } catch (error) {
            this.showError('Ошибка удаления сотрудника', error);
        }
    }

    // Вспомогательные методы
    populateUserSelect(users) {
        const select = document.getElementById('employee-user-select');
        select.innerHTML = users.map(user => `
            <option value="${user.id}">${user.surname} ${user.name} (@${user.username})</option>
        `).join('');
    }

    static showSuccess(message) {
        notification.className = 'success';
        showNotification(message);
    }

    static showError(message, error) {
        notification.className = 'error';
        showNotification(message + ' ' + error);
    }

    
    static async getCurrentUserInfo() {
        try {
            const user = await API.get('/api/auth/me');
            return user;
        } catch (error) {
            this.showError('Ошибка загрузки данных о пользователе', error);
        }
    }
}