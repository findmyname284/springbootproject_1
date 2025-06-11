class ManagerPanel {
    constructor() {
        this.loadInitialData();
    }

    async loadInitialData() {
        try {
            const [clients, suppliers, warehouses, employees, categories] = await Promise.all([
                API.get('/api/manager/clients'),
                API.get('/api/manager/suppliers'),
                API.get('/api/manager/warehouses'),
                API.get('/api/manager/employees'),
                API.get('/api/manager/categories')
            ]);

            this.renderClients(clients);
            this.renderSuppliers(suppliers);
            this.renderWarehouses(warehouses);
            this.populateWarehousesSelect(warehouses);
            this.renderEmployees(employees);
            this.populateEmployeesSelect(employees);
            this.loadUsersWithoutEmployee();
            this.renderCategories(categories);
        } catch (error) {
            notification.className = 'error';
            showNotification('Ошибка загрузки данных' + error);
        }
    }

    populateWarehousesSelect(warehouses) {
        const selects = document.querySelectorAll('.warehouse-select');
        selects.forEach(select => {
            select.innerHTML = warehouses.map(warehouse => `<option value="${warehouse.id}">${warehouse.name}</option>`).join('');
        });
    }

    populateEmployeesSelect(employees) {
        const select = document.querySelector('.employee-select');
        select.innerHTML = employees.map(employee => `<option value="${employee.id}">${employee.user.surname + ' ' + employee.user.name}</option>`).join('');
    }

    renderClients(clients) {
        const container = document.getElementById('clients-list');
        container.innerHTML = clients.map(client => `
            <div class="user-card" onclick="ManagerPanel.openClientEditForm(${client.id})" data-client-id="${client.id}">
                <h3>${client.surname} ${client.name}</h3>
                <p>${client.email}</p>
                <p>${client.phone}</p>
                <p>${client.balance} ₸</p>
                <p>${client.address}</p>
            </div>
        `).join('');
    }

    renderSuppliers(suppliers) {
        const container = document.getElementById('suppliers-list');
        container.innerHTML = suppliers.map(supplier => `
            <div class="user-card" data-supplier-id="${supplier.id}">
                <h3>${supplier.name}</h3>
                <p>${supplier.email}</p>
                <p>${supplier.phone}</p>
                <p>${supplier.address}</p>
                <div class="card-actions">
                    <button class="btn-icon" onclick="ManagerPanel.openSupplierEditForm(${supplier.id})">✏️</button>
                    <button class="btn-icon danger" onclick="ManagerPanel.deleteSupplier(${supplier.id})">🗑️</button>
                </div>
            </div>
        `).join('');
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

    static openSupplierEditForm(id) {
        Modal.open('supplier-edit-modal');
        document.body.style.overflow = 'hidden';
        API.get(`/api/manager/suppliers/${id}`).then(supplier => {
            const form = document.getElementById('supplier-edit-form');
            form.querySelector('input[name="id"]').value = supplier.id || '';
            form.querySelector('input[name="name"]').value = supplier.name || '';
            form.querySelector('input[name="email"]').value = supplier.email || '';
            form.querySelector('input[name="phone"]').value = supplier.phone || '';
            form.querySelector('input[name="address"]').value = supplier.address || '';
        });
    }


    static deleteSupplier(id) {
        if (confirm('Вы действительно хотите удалить поставщика?')) {
            API.delete(`/api/manager/suppliers/${id}`).then(data => {
                if (data.error) {
                    notification.className = 'error';
                    showNotification(data.error);
                    return;
                }
                Modal.close('supplier-edit-modal');
                notification.className = 'success';
                showNotification('Поставщик успешно удален');
                window.location.reload();
            });
        }
    }

    static openClientEditForm(id) {
        Modal.open('client-edit-modal');
        document.body.style.overflow = 'hidden';
        API.get(`/api/manager/clients/${id}`).then(client => {
            const form = document.getElementById('client-edit-form');
            form.querySelector('input[name="id"]').value = client.id || '';
            form.querySelector('input[name="balance"]').value = client.balance || '';
            form.querySelector('input[name="username"]').value = client.username || '';
            form.querySelector('input[name="name"]').value = client.name || '';
            form.querySelector('input[name="surname"]').value = client.surname || '';
            form.querySelector('input[name="email"]').value = client.email || '';
            form.querySelector('input[name="phone"]').value = client.phone || '';
            form.querySelector('input[name="address"]').value = client.address || '';
        });
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
            form.querySelector('select[name="employee-id"]').value = warehouse.manager?.id || '';
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
                <p>${employee.user.email} · ${employee.user.phone}</p>
                <p>ИИН: ${employee.iin}</p>
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

    renderCategories(categories) {
        const container = document.getElementById('categories-list');
        container.innerHTML = categories.map(category => `
            <div class="category-card" data-employee-id="${category.id}">
                <h3>${category.name}</h3>
                <div class="card-actions">
                    <button class="btn-icon" onclick="ManagerPanel.openCategoryEditForm(${category.id})">✏️</button>
                    <button class="btn-icon danger" onclick="ManagerPanel.deleteCategory(${category.id})">🗑️</button>
                </div>
            </div>
        `).join('');
    }

    static async addCategory() {
        try {
            const formData = new FormData(event.target);
            await API.post('/api/manager/categories', Object.fromEntries(formData)).then(data => {
                if (data.error) {
                    this.showError('Ошибка добавления категории', data.error);
                    return;
                } else if (data.success) {
                    Modal.close('category-modal');
                    this.showSuccess(data.success);
                    window.location.reload();
                }
            });
        } catch (error) {
            this.showError('Ошибка добавления категории', error);
        }
    }

    static async updateCategory() {
        const formData = new FormData(event.target);
        const id = formData.get('id');
        try {
            await API.put(`/api/manager/categories/${id}`, Object.fromEntries(formData)).then(data => {
                if (data.error) {
                    this.showError('Ошибка обновления категории', data.error);
                    return;
                }
                Modal.close('category-edit-modal');
                this.showSuccess(data.success);
                window.location.reload();
            });
        } catch (error) {
            this.showError('Ошибка обновления категории', error);
        }
    }

    static async openCategoryEditForm(id) {
        try {
            await API.get(`/api/manager/categories/${id}`).then(category => {
                if (category.error) {
                    this.showError('Ошибка открытия формы редактирования категории', category.error);
                    return;
                }
                Modal.open('category-edit-modal');
                const form = document.getElementById('category-edit-form');
                form.querySelector('input[name="id"]').value = id;
                form.querySelector('input[name="name"]').value = category.name;
            });
        } catch (error) {
            this.showError('Ошибка открытия формы редактирования категории', error);
        }
    }

    static async deleteCategory(id) {
        if (!confirm('Вы уверены что хотите удалить категорию?')) return;

        try {
            await API.delete(`/api/manager/categories/${id}`).then(data => {
                if (data.error) {
                    this.showError(data.error, null);
                    return;
                }
                Modal.close('category-edit-modal');
                this.showSuccess(data.success);
                window.location.reload();
            });
        } catch (error) {
            this.showError('Ошибка удаления сотрудника', error);
        }
    }
}