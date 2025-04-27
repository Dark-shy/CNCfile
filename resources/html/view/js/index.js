$(document).ready(function () {
    // 检查登录状态
    if (localStorage.getItem('loggedIn') !== 'true') {
        // 如果未登录，重定向到登录页面
        window.location.href = 'login.html';
    }

    $(document).ready(function () {
        $('[data-toggle="tooltip"]').tooltip();
    });
    var ip = 'http://10.10.10.104:8080';


    var workcode = localStorage.getItem('workcode');
    var Name;
    // console.log(workcode);
    // 获取用户信息
    $.ajax({
        url: ip + '/user/' + workcode,
        type: 'GET',
        success: function (response) {
            $('#addUser').val(response.userName).prop('readonly', true);
            $('#department').val(response.userSector).prop('readonly', true);
            Name = response.userName;
        },
        error: function (error) {
            alert('获取用户信息失败');
        }
    });

    $('#changePasswordBtn').click(showEditor);

    //初始模态框
    function showEditor() {
        $('#dataForm')[0].reset();
        $('#dataForm').removeClass('was-validated');
        $('#myModal').modal('show');
        $('#myModal').modal('show').on('shown.bs.modal', function () {
            $('#username').val(Name).prop('readonly', true);
        });
    }

    // 检查用户角色
    var role = localStorage.getItem('role');
    if (role !== 'user1' || role === null) {
        // var findfileTab = document.getElementById('findfile-tab');
        var addfileTab = document.getElementById('addfile-tab');
        var approveTab = document.getElementById('approvefile-tab');
        var downloadTab = document.getElementById('downloadfile-tab');
        // var findfileContent = document.getElementById('findfile');
        // var addfileContent = document.getElementById('addfile');
        // var approveContent = document.getElementById('approve');
        // var downloadContent = document.getElementById('download');
        if (role === 'user3' || role === null) {
            if (addfileTab) {
                addfileTab.style.display = 'none';
                // addfileContent.style.display = 'none';
            }
            if (approveTab) {
                approveTab.style.display = 'none';
                // approveContent.style.display = 'none';
            }
        } else if (role === 'user2') {
            if (approveTab) {
                approveTab.style.display = 'none';
                // approveContent.style.display = 'none';
            }
        }
        if (role === null) {
            if (downloadTab) {
                downloadTab.style.display = 'none';
                // downloadContent.style.display = 'none';
            }
        }
    }

    // 查询程序
    $('#searchForm').on('submit', function (event) {
        event.preventDefault();
        var partNumber = $('#findPartNumber').val();
        var addTime = $('#addTime').val();
        var state = $('#state').val();
        loadProgramRecords(partNumber, addTime, state);
    });

    //更改密码
    $('#changePassword').click(function () {
        // 禁用按钮防重复提交
        $(this).prop('disabled', true);

        // 收集表单数据
        const formData = {
            username: workcode,
            password: $('#password').val(),
            role: null
        };

        // 发送请求
        $.ajax({
            url: ip + '/update/user',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(formData),
            success: function () {
                alert('修改成功');
                $('#myModal').modal('hide');
            },
            error: function (xhr) {
                alert('错误: ' + xhr.responseJSON?.message);
            },
            complete: function () {
                $('#changePassword').prop('disabled', false);
            }
        });
    });

    function loadProgramRecords(partNumber, addTime, state) {
        $.ajax({
            url: ip + '/file/' + partNumber,
            type: 'GET',
            success: function (response) {
                var records = response;
                var tbody = $('#programRecords');
                tbody.empty();

                // 按 addTime 排序，确保最新的记录在最前面
                records.sort(function (a, b) {
                    return new Date(b.addTime || 0) - new Date(a.addTime || 0);
                });

                // 筛选符合条件的记录
                var filteredRecords = records.filter(function (record) {
                    var matchAddTime = addTime ? new Date(record.addTime).toLocaleDateString() === new Date(addTime).toLocaleDateString() : true;
                    var matchState = state ? record.state.toString() === state : true;
                    return matchAddTime && matchState;
                });

                // 显示所有符合条件的记录
                filteredRecords.forEach(function (record, index) {
                    var stateText = record.state === "0" ? '否' : '是';
                    var unlockButtonClass = record.state === "0" ? 'btn-danger' : 'btn-success';
                    var unlockButtonText = record.state === "0" ? '锁定' : '解锁';
                    var disabledAttr = (role !== 'user1') ? 'disabled' : '';
                    var row = '<tr>' +
                        '<td>' + (index + 1) + '</td>' +
                        '<td>' + record.categoryId + '</td>' +
                        '<td class="text-truncate" data-toggle="tooltip" data-placement="top" title="' + record.categoryName + '">' + record.categoryName + '</td>' +
                        '<td class="text-truncate" data-toggle="tooltip" data-placement="top" title="' + record.specification + '">' + record.specification + '</td>' +
                        '<td>' + record.engineer + '</td>' +
                        '<td>' + (new Date(record.addTime) || '无日期').toLocaleDateString() + '</td>' +
                        '<td>' + stateText + '</td>' +
                        '<td class="text-truncate" data-toggle="tooltip" data-placement="top" title="' + record.procedureName + '">' + record.procedureName + '</td>' +
                        '<td>' + record.versions + '</td>' +
                        '<td>' + record.type + '</td>' +
                        '<td>' + record.addUser + '</td>' +
                        '<td><button class="btn ' + unlockButtonClass + ' btn-sm change-state" data-file-id="' + record.fileId + '" data-current-state="' + record.state + '" ' + disabledAttr + '>' + unlockButtonText + '</button></td>' +
                        '</tr>';
                    tbody.append(row);
                });
            },
            error: function (error) {
                alert('加载程序记录失败');
            }
        });
    }

    // 使用事件委托绑定状态变更事件
    $(document).on('click', '.change-state', function () {
        const fileId = $(this).data('file-id');
        const currentState = $(this).data('current-state');
        changeState(fileId, currentState);
    });

    function changeState(fileId, currentState) {
        // 实现状态变更功能
        var newState = currentState === "0" ? 1 : 0;
        $.ajax({
            url: ip + '/file/state/' + fileId,
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify({state: newState}),
            success: function (response) {
                alert('状态变更成功');
                // 重新加载记录
                $('#searchForm').submit();
            },
            error: function (error) {
                alert('状态变更失败');
            }
        });
    }

    // 上传程序
    document.getElementById('date').value = new Date().toLocaleDateString();
    $('#uploadForm').on('submit', function (event) {
        event.preventDefault();
        var formData = new FormData();
        formData.append('addUser', $('#addUser').val());
        formData.append('department', $('#department').val());
        formData.append('date', $('#date').val());
        formData.append('engineer', $('#engineer').val());
        formData.append('categoryId', $('#partNumber').val());
        formData.append('categoryName', $('#partName').val());
        formData.append('specification', $('#specification').val());
        formData.append('type', $('#type').val());
        formData.append('state', "2"); // 默认数据

        var fileInput = $('#file')[0];
        if (fileInput.files.length > 0) {
            var file = fileInput.files[0];
            var encodedFileName = encodeURIComponent(file.name); // 对文件名进行编码
            formData.append('file', file, encodedFileName); // 将编码后的文件名传入
        }

        $.ajax({
            url: ip + '/file',
            type: 'POST',
            contentType: false,
            processData: false,
            data: formData,
            success: function (response) {
                alert('提交成功');
                // 处理成功响应
            },
            error: function (error) {
                alert('提交失败');
                // 处理错误响应
            }
        });
    });

    $('#partNumber').on('change', function () {
        var partNumber = $(this).val();
        if (partNumber) {
            loadHistoryRecords(partNumber);
            loadCategoryDetails(partNumber);
        }
    });

    function loadHistoryRecords(partNumber) {
        $.ajax({
            url: ip + '/file/' + partNumber,
            type: 'GET',
            success: function (response) {
                var records = response;
                var tbody = $('#historyRecords');
                tbody.empty();
                records.forEach(function (record) {
                    var row = '<tr>' +
                        '<td>' + record.categoryId + '</td>' +
                        '<td class="text-truncate" data-toggle="tooltip" data-placement="top" title="' + record.categoryName + '">' + record.categoryName + '</td>' +
                        '<td class="text-truncate" data-toggle="tooltip" data-placement="top" title="' + record.specification + '">' + record.specification + '</td>' +
                        '<td>' + record.addUser + '</td>' +
                        '<td>' + record.engineer + '</td>' +
                        '<td>' + (new Date(record.addTime) || '无日期').toLocaleDateString() + '</td>' +
                        '<td>' + record.versions + '</td>' +
                        '<td class="text-truncate" data-toggle="tooltip" data-placement="top" title="' + record.procedureName + '">' + record.procedureName + '</td>' +
                        '</tr>';
                    tbody.append(row);
                });
            },
            error: function (error) {
                alert('加载历史记录失败');
            }
        });
    }

    function loadCategoryDetails(partNumber) {
        $.ajax({
            url: ip + '/category/' + partNumber,
            type: 'GET',
            success: function (response) {
                if (response && response.categoryName && response.specification) {
                    $('#partName').val(response.categoryName).prop('readonly', true);
                    $('#specification').val(response.specification).prop('readonly', true);
                } else {
                    alert('未查到相关品类');
                    $('#partName').val('').prop('readonly', false);
                    $('#specification').val('').prop('readonly', false);
                }
            },
            error: function (error) {
                alert('加载品类详情失败');
                $('#partName').val('').prop('readonly', false);
                $('#specification').val('').prop('readonly', false);
            }
        });
    }

    // 更新文件名显示
    $('#file').on('change', function () {
        var fileName = $(this).val().split('\\').pop();
        $(this).next('.custom-file-label').html(fileName);
    });

    // 下载程序
    $('#downloadSearchForm').on('submit', function (event) {
        event.preventDefault();
        var partNumber = $('#downloadPartNumber').val();
        if (partNumber) {
            loadDownloadProgramRecords(partNumber);
        }
    });

    function loadDownloadProgramRecords(partNumber) {
        $.ajax({
            url: ip + '/file/' + partNumber,
            type: 'GET',
            success: function (response) {
                var records = response;
                var tbody = $('#downloadProgramRecords');
                tbody.empty();

                // 按 addTime 排序，确保最新的记录在最前面
                records.sort(function (a, b) {
                    return new Date(b.addTime || 0) - new Date(a.addTime || 0);
                });

                // 只显示最新的记录
                if (records.length > 0) {
                    var record = records[0];
                    var stateText = record.state === "0" ? '否' : '是';
                    var downloadButtonClass = record.state === "0" ? 'btn-primary' : 'btn-secondary';
                    var downloadButtonDisabled = record.state === "0" ? '' : 'disabled';
                    var row = '<tr>' +
                        '<td>' + record.categoryId + '</td>' +
                        '<td class="text-truncate" data-toggle="tooltip" data-placement="top" title="' + record.categoryName + '">' + record.categoryName + '</td>' +
                        '<td class="text-truncate" data-toggle="tooltip" data-placement="top" title="' + record.specification + '">' + record.specification + '</td>' +
                        '<td>' + record.versions + '</td>' +
                        '<td>' + record.engineer + '</td>' +
                        '<td class="text-truncate" data-toggle="tooltip" data-placement="top" title="' + record.procedureName + '">' + record.procedureName + '</td>' +
                        '<td><button class="btn ' + downloadButtonClass + ' btn-sm download-program" data-category-id="' + record.procedureName + '" ' + downloadButtonDisabled + '>下载</button></td>' +
                        '<td>' + stateText + '</td>' +
                        '</tr>';
                    tbody.append(row);
                }
            },
            error: function (error) {
                alert('加载程序记录失败');
            }
        });
    }

    // 使用事件委托处理下载
    $(document).on('click', '.download-program', function () {
        var categoryId = $(this).data('category-id');
        downloadProgram(categoryId);
    });

    function downloadProgram(categoryId) {
        // 实现下载功能
        window.location.href = ip + '/file/download/?procedureName=' + categoryId;
    }

    // 文件审批
    function loadApproveRecords() {
        $.ajax({
            url: ip + '/file/admin',
            type: 'GET',
            success: function (response) {
                var records = response;
                var tbody = $('#approveRecords');
                tbody.empty();
                records.forEach(function (record) {
                    var row = '<tr>' +
                        '<td>' + record.categoryId + '</td>' +
                        '<td class="text-truncate" data-toggle="tooltip" data-placement="top" title="' + record.categoryName + '">' + record.categoryName + '</td>' +
                        '<td class="text-truncate" data-toggle="tooltip" data-placement="top" title="' + record.specification + '">' + record.specification + '</td>' +
                        '<td>' + record.versions + '</td>' +
                        '<td>' + record.engineer + '</td>' +
                        '<td class="text-truncate" data-toggle="tooltip" data-placement="top" title="' + record.procedureName + '">' + record.procedureName + '</td>' +
                        '<td>' +
                        '<button class="btn btn-success btn-sm approve-file" data-file-id="' + record.fileId + '">通过</button>' +
                        '<button class="btn btn-danger btn-sm disapprove-file ml-2" data-file-id="' + record.fileId + '">不通过</button>' +
                        '</td>' +
                        '</tr>';
                    tbody.append(row);
                });
            },
            error: function (error) {
                alert('加载审批记录失败');
            }
        });
    }

    // 绑定审批按钮事件
    $(document).on('click', '.approve-file', function () {
        var row = $(this).closest('tr');
        var record = getRecordFromRow(row);
        record.state = 0; // 通过
        approveFile(record);
    });

    $(document).on('click', '.disapprove-file', function () {
        var row = $(this).closest('tr');
        var record = getRecordFromRow(row);
        record.state = 4; // 不通过
        approveFile(record);
    });

    function getRecordFromRow(row) {
        return {
            categoryId: row.find('td').eq(0).text(),
            categoryName: row.find('td').eq(1).text(),
            specification: row.find('td').eq(2).text(),
            versions: row.find('td').eq(3).text(),
            engineer: row.find('td').eq(4).text(),
            procedureName: row.find('td').eq(5).text(),
            fileId: row.find('.approve-file, .disapprove-file').data('file-id')
        };
    }

    function approveFile(record) {
        $.ajax({
            url: ip + '/file',
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(record),
            success: function (response) {
                alert('审批成功');
                loadApproveRecords();
            },
            error: function (error) {
                alert('审批失败');
            }
        });
    }

    // 加载审批记录
    loadApproveRecords();
});