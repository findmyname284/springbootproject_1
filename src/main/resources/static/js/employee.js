document.addEventListener('DOMContentLoaded', () => {
    let photoUrl = "";
    const dropZones = document.querySelectorAll('.drop-zone');
    const previews = document.querySelectorAll('.preview');
    const modalOverlays = document.querySelectorAll('.modal-overlay');
    const editProductForm = document.getElementById("productEditForm");
    const сategorySelect = document.querySelectorAll(".сategory-select");

    dropZones.forEach(dropZone => {
        const fileInput = dropZone.querySelector('#fileInput');

        dropZone.addEventListener('click', () => fileInput.click());
        fileInput.addEventListener('change', (e) => handleFiles(e));

        dropZone.addEventListener('dragover', (e) => {
            e.preventDefault();
            dropZone.classList.add('dragover');
        });

        dropZone.addEventListener('dragleave', () => {
            dropZone.classList.remove('dragover');
        });

        dropZone.addEventListener('drop', (e) => {
            e.preventDefault();
            dropZone.classList.remove('dragover');
            const files = e.dataTransfer.files;
            if (files.length) handleFiles({ target: { files } });
        });
    });

    async function handleFiles(e) {
        photoUrl = "";
        const files = e.target.files;
        for (const file of files) {
            if (!file.type.startsWith('image/')) {
                alert('Пожалуйста, выбирайте только изображения');
                continue;
            }

            const reader = new FileReader();
            reader.onload = () => {
                let img = `<img src="${reader.result}" class="preview-image" style="max-width: 100px; max-height: 100px;" />`;
                previews.forEach(previewEl => {
                    previewEl.innerHTML = img;
                });
                console.log("Image Preview Updated");
            };
            reader.readAsDataURL(file);

            try {
                const formData = new FormData();
                formData.append('file', file);

                const response = await fetch('/api/products/upload', {
                    method: 'POST',
                    body: formData
                });

                const data = await response.json();
                if (!response.ok) throw new Error(data.error);

                photoUrl = data.url;
            } catch (error) {
                console.error('Upload error:', error);
                alert('Ошибка загрузки: ' + error.message);
            }
        }
    }

    document.getElementById('productForm').addEventListener('submit', async (e) => {
        e.preventDefault();

        const formData = new FormData(e.target);
        formData.append('image', photoUrl);

        try {
            const response = await fetch('/api/products', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(Object.fromEntries(formData))
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Ошибка при сохранении');
            }

            notification.className = 'success';
            showNotification('Продукт успешно добавлен!');
            alert('Продукт успешно добавлен!');
            window.location.reload();
            document.getElementById('productForm').reset();

        } catch (error) {
            console.error('Ошибка:', error);
            alert(`Ошибка: ${error.message}`);
        }
    });

    document.getElementById('productEditForm').addEventListener('submit', async (e) => {
        e.preventDefault();

        const formData = new FormData(e.target);
        formData.append('image', photoUrl);

        try {
            API.put("/api/products", Object.fromEntries(formData))
                .then(data => {
                    if (data.error) {
                        notification.className = 'error';
                        showNotification(data.error);
                        return;
                    }
                    notification.className = 'success';
                    showNotification(data.success);
                    Modal.close('product-edit-modal');
                    window.location.reload();
                    preview.innerHTML = '';
                    document.getElementById('productEditForm').reset();
                })
        } catch (error) {
            notification.className = 'error';
            showNotification(error);
        }
    });

    modalOverlays.forEach(overlay => {
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                Modal.close(overlay.id);
            }
        });
    });


    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            modalOverlays.forEach(overlay => {
                Modal.close(overlay.id);
            });
        }
    });

    editProduct = (id) => {
        Modal.open('product-edit-modal');
        API.get(`/api/products/get?id=${id}`)
            .then(data => {
                editProductForm.querySelector('input[name="id"]').value = data.id;
                editProductForm.querySelector('input[name="name"]').value = data.name;
                editProductForm.querySelector('textarea[name="description"]').value = data.description;
                editProductForm.querySelector('input[name="sku"]').value = data.sku;
                editProductForm.querySelector('input[name="basePrice"]').value = data.basePrice;
                editProductForm.querySelector('input[name="quantity"]').value = data.quantity;
                editProductForm.querySelector('select[name="categoryId"]').value = data.category.id;

                photoUrl = data.image ?? "/img/no-image.jpg";
                photoUrl = photoUrl == 'null' ? '/img/no-image.png' : photoUrl;
                if (photoUrl) {
                    const preview = editProductForm.querySelector('#preview');
                    preview.innerHTML = `<img src="${photoUrl} " class="preview-image" />`;
                }
            })
            .catch(error => console.error('Ошибка:', error));
    }

    deleteProduct = (id) => {
        API.delete(`/api/products/${id}`)
            .then(data => {
                if (data.error) {
                    notification.className = 'error';
                    showNotification(data.error);
                    return;
                }
                notification.className = 'success';
                showNotification(data.success);
                window.location.reload();
            })
            .catch(error => console.error('Ошибка:', error));
    }

    сategorySelect.forEach(select => {
        API.get('/api/manager/categories')
            .then(data => {
                data.forEach(category => {
                    const option = document.createElement('option');
                    option.value = category.id;
                    option.textContent = category.name;
                    select.appendChild(option);
                });
            })
            .catch(error => console.error('Ошибка:', error));
    })
});