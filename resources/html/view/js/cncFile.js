/******************** 全局配置 ********************/
var ip = 'http://10.10.10.104:8080';
let fileCounter = 0;

var workcode = localStorage.getItem('workcode') ?? sessionStorage.getItem('workcode');
console.log(workcode);
// 获取用户信息
$.ajax({
    url: ip + '/user/' + workcode,
    type: 'GET',
    success: function (response) {
        $('#applicant').val(response.userName).prop('readonly', true);
        $('#department').val(response.userSector).prop('readonly', true);
        $('#engineer').val(response.userName);
    },
    error: function (error) {
        alert('获取用户信息失败');
    }
});
/******************** 工具函数 ********************/
// 安全获取DOM值
const getSafeValue = (selector, defaultValue = '') => {
    const element = document.querySelector(selector);
    return element?.value ?? defaultValue;
};
// 版本号解析工具函数
const parseVersion = (versionStr) => {
    const match = versionStr.match(/^([A-Z])(\d+)-(\d+)$/);
    return match ? {
        prefix: match[1],
        major: parseInt(match[2]),
        minor: parseInt(match[3])
    } : null;
};
let historyData = []; // 存储从服务器获取的历史记录
let maxVersions = []; // 存储从服务器获取的各个版本最大值
const getMaxVersionsByPrefix = (historyData) => {
    const maxVersions = {};

    // 遍历所有历史记录
    historyData.forEach(product => {
        product.details.forEach(detail => {
            // 解析版本号（如 "P1.2" → { prefix: 'P', major:1, minor:2 }）
            const version = parseVersion(detail.version);
            if (!version) return; // 忽略无效版本

            const {prefix, major, minor} = version;

            // 若前缀不存在或当前版本更大，则更新最大值
            if (!maxVersions[prefix] ||
                major > maxVersions[prefix].major ||
                (major === maxVersions[prefix].major && minor > maxVersions[prefix].minor)) {
                maxVersions[prefix] = {major, minor};
            }
        });
    });

    return maxVersions;
};

// 核心行数据更新
function updateRowData(row) {
    if (!row) return;

    // 输入清理
    const inputs = {
        productCode: getSafeValue('#partNumber', '').trim().replace(/\s+/g, '_'),
        modelName: getSafeValue('#machineName', '').trim().replace(/\s+/g, '_'),
        plateThickness: `${getSafeValue('#thickness', '0').replace(/[^0-9.]/g, '')}mm`,
        plateMaterial: getSafeValue('#material', '').replace(/板材/g, '').trim(),
        versionPrefix: getSafeValue('#versionPrefix', 'A'),
        versionType: getSafeValue('#versionType', '升版'),
        scatteredType: row.querySelector('select.scattered-type')?.value
    };

    // 更新显示内容
    const safeUpdate = (selector, value) => {
        const element = row.querySelector(selector);
        if (element) element.textContent = value;
    };

    safeUpdate('.displayPartNumber', inputs.productCode);
    safeUpdate('.displayPartName', getSafeValue('#partName', '').trim());
    safeUpdate('.displaySpecification', getSafeValue('#specification', '').trim());
    safeUpdate('.displayMachineName', inputs.modelName);
    safeUpdate('.displayThickness', inputs.plateThickness);
    safeUpdate('.displayMaterial', inputs.plateMaterial);
    safeUpdate('.displayVersionPrefix', inputs.versionPrefix);

    // 版本号生成逻辑
    const generateVersion = () => {
        // 过滤当前品号且相同散板类型的历史记录（新增散板类型过滤）
        const currentProductHistory = historyData.filter(item =>
            item.productCode === inputs.productCode &&
            item.details.some(detail => detail.scatteredType === inputs.scatteredType)  // 新增散板类型条件
        );

        // 获取当前前缀的所有版本（后续逻辑保持不变但作用域已限定到特定散板类型）
        const versions = currentProductHistory
            .flatMap(record => record.details)
            .map(detail => parseVersion(detail.version))
            .filter(v => v?.prefix === inputs.versionPrefix);

        //获取当前品号最大版本号
        // const currentMax = maxVersions[inputs.versionPrefix] || { major: 0, minor: 0 };

        // 获取最新版本（逻辑不变，但基于当前散板类型的数据）
        const latest = versions.reduce((acc, curr) => {
            if (curr.major > acc.major ||
                (curr.major === acc.major && curr.minor > acc.minor)) {
                return curr;
            }
            return acc;
        }, {major: 0, minor: 0}); // 初始值建议设为 0.0

        // 根据版本类型生成候选版本号
        let candidate;
        if (inputs.versionType === '升版') {
            candidate = {major: latest.major + 1, minor: 0};
        } else {
            candidate = {major: latest.major, minor: latest.minor + 1};
        }
        // 处理无历史数据的情况（新增逻辑）
        if (versions.length === 0) {
            // document.getElementById('cadFiles').required = true;
            return inputs.versionType === '升版'
                ? {major: 0, minor: 0}  // 该散板类型首版本
                : {major: 0, minor: 1};
        }
        // if (candidate.major > currentMax.major ||
        //     (candidate.major === currentMax.major && candidate.minor > currentMax.minor)) {
        //     document.getElementById('cadFiles').required = true;
        // }

        return candidate;
        // // 生成新版本号（原有逻辑保持不变）
        // if (inputs.versionType === '升版') {
        //     return { major: latest.major + 1, minor: 0 };
        // }
        // return { major: latest.major, minor: latest.minor + 1 };
    };
    // 生成版本号（带默认值）
    const baseVersion = generateVersion() || {major: 0, minor: 0};
    const version = `${inputs.versionPrefix}${baseVersion.major}-${baseVersion.minor}`;

    // 生成程序名
    safeUpdate('.displayVersionPrefix', version);
    safeUpdate('.displayProgramName', `${inputs.productCode}_${inputs.modelName}_${inputs.scatteredType}_${version}_${inputs.plateThickness}_${inputs.plateMaterial}`.replace(/\s+/g, ''));
}

//CAD文件上传控制逻辑

/********************文件名校验*************************/

//文件校验
const validateProgramFile = (input) => {
    const row = input.closest('tr');
    const programName = row.querySelector('.displayProgramName').textContent.trim();
    const file = input.files[0];

    // 严格匹配逻辑
    const escapedName = programName.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const regex = new RegExp(
        `^${escapedName}\\.(ino|pde|nc|gcode|tap|ard)$`, // 精确扩展名匹配
        'i' // 扩展名不区分大小写
    );

    if (!file || !regex.test(file.name)) {
        input.value = '';
        alert(`文件名必须为：${programName}.扩展名（允许.ino/.pde/.nc/.gcode/.tap/.ard）`);
        return false;
    }
    return true;
};
/******************** CAD文件校验逻辑 ********************/
const validateCADFiles = (input) => {
    const partNumber = document.getElementById('partNumber').value.trim();
    const files = Array.from(input.files);

    // 前置校验：产品编号是否存在
    if (!partNumber) {
        alert('请先填写产品编号');
        input.value = '';
        return false;
    }

    // 构建动态正则表达式
    const escapedPN = partNumber.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const prefixRegex = new RegExp(
        `${escapedPN}`, // 严格匹配前缀
    );
    const extRegex = /\.(dwg|dxf|bak)$/i;

    // 执行校验
    const invalidFiles = files.filter(file => {
        const name = file.name.toUpperCase();
        return !(
            prefixRegex.test(name) && // 前缀匹配
            extRegex.test(name)       // 扩展名匹配
        );
    });

    // 错误处理
    if (invalidFiles.length > 0) {
        const errorNames = invalidFiles.map(f => f.name).join(', ');
        input.value = '';
        alert(`CAD文件命名规则：
1. 以品号 开头
2. 扩展名为 .dwg/.dxf/.bak
示例：${partNumber}_....扩展名
错误文件：${errorNames}`);
        return false;
    }

    return true;
};

/******************** 事件绑定修改 ********************/
document.addEventListener('DOMContentLoaded', () => {
    // CAD文件校验绑定
    document.getElementById('cadFiles').addEventListener('change', function () {
        const isValid = validateCADFiles(this);
        if (!isValid) {
            this.nextElementSibling.textContent = "选择CAD图纸文件";
            return;
        }
        // 更新显示
        this.nextElementSibling.textContent =
            `已选 ${this.files.length} 个合规CAD文件`;
    });
});
/******************** DOM初始化 ********************/
document.addEventListener('DOMContentLoaded', () => {
    // 获取初始行的下拉菜单
    const initialSelect = document.querySelector('#programRecords tr select.scattered-type');

    // 绑定change事件
    initialSelect.addEventListener('change', () => {
        updateRowData(initialSelect.closest('tr'));
    });

    // 初始数据更新
    updateRowData(initialSelect.closest('tr'));
    /****************************************/
    // 程序文件校验（初始行+动态行）
    // document.querySelectorAll('.file-upload-input').forEach(input => {
    //     input.addEventListener('change', function () {
    //         if (!validateProgramFile(this)) return;
    //         // 更新显示
    //         this.previousElementSibling.textContent =
    //             this.files[0].name.substring(0, 12) + '...';
    //     });
    // });

    // CAD文件校验
    document.getElementById('cadFiles').addEventListener('change', function () {
        if (!validateCADFiles(this)) return;
        // 更新文件计数显示
        this.nextElementSibling.textContent =
            `已选 ${this.files.length} 个CAD文件`;
    });
    /****************************************/

    // 初始化和动态行统一调用
    // document.querySelectorAll('.file-upload-input').forEach(setupFileInput); 

    // 初始化日期
    const dateInput = document.getElementById('date');
    if (dateInput) dateInput.valueAsDate = new Date();

    // 动态行管理
    const programRecords = document.getElementById('programRecords');
    programRecords.addEventListener('click', function (e) {
        const target = e.target;

        // 添加新行
        if (target.classList.contains('addRow')) {
            const newRow = document.createElement('tr');
            fileCounter++;

            newRow.innerHTML = `
                <td><span class="displayPartNumber"></span></td>
                <td><span class="displayPartName"></span></td>
                <td><span class="displaySpecification"></span></td>
                <td><span class="displayMachineName"></span></td>
                <td><span class="displayThickness"></span></td>
                <td><span class="displayMaterial"></span></td>
                <td>
                    <select class="form-control form-control-sm scattered-type">
                        <option value="前板">前板</option>
                        <option value="后板">后板</option>
                    </select>
                </td>
                <td><span class="displayVersionPrefix"></span></td>
                <td><span class="displayProgramName">待生成</span></td>
                <td>
                    <div class="file-upload-btn">
                        <button type="button" class="btn btn-primary btn-sm">选择文件</button>
                        <input type="file" class="file-upload-input" 
                            data-file-id="file${fileCounter}" 
                            accept=".ino,.pde,.nc,.gcode,.tap,.ard" 
                            required>
                    </div>
                </td>
                <td><input type="text" class="form-control form-control-sm"></td>
                <td>
                    <button type="button" class="btn btn-outline-success btn-sm addRow">＋</button>
                    <button type="button" class="btn btn-outline-danger btn-sm removeRow">－</button>
                </td>
            `;

            // 绑定事件
            newRow.querySelector('select.scattered-type').addEventListener('change', () => {
                updateRowData(newRow);
            });

            const currentProductCode = getSafeValue('#partNumber', '').trim();
            if (currentProductCode) {
                fetchScatteredTypes(currentProductCode, newRow);
            }
            // const fileInput = newRow.querySelector('.file-upload-input');
            // fileInput.addEventListener('change', function () {
            //     const displayBtn = this.previousElementSibling;

            //     if (displayBtn) {
            //         displayBtn.textContent = this.files[0]?.name.substring(0, 15) || '未选择文件';
            //     }
            // });

            this.appendChild(newRow);
            updateRowData(newRow);

        }

        // 删除行
        if (target.classList.contains('removeRow')) {
            const row = target.closest('tr');
            if (row && programRecords.querySelectorAll('tr').length > 1) {
                row.remove();
                programRecords.querySelectorAll('tr').forEach(updateRowData);
            }
        }
    });

    // 字段变更监听
    let updateTimeout;
    const updateFields = [
        '#partNumber', '#partName', '#specification',
        '#machineName', '#thickness', '#material',
        '#versionPrefix', '#versionType'
    ];
    updateFields.forEach(selector => {
        const element = document.querySelector(selector);
        if (element) {
            element.addEventListener('input', () => {
                clearTimeout(updateTimeout);
                updateTimeout = setTimeout(() => {
                    programRecords.querySelectorAll('tr').forEach(updateRowData);
                }, 300);
            });
        }
    });


    // 表单提交
    document.getElementById('uploadForm').addEventListener('submit', function (e) {
        e.preventDefault();

        // 验证文件
        const invalidFiles = Array.from(document.querySelectorAll('.file-upload-input'))
            .filter(input => !input.files?.length)
            .map(input => input.dataset.fileId.replace('file', ''));

        if (invalidFiles.length > 0) {
            alert(`以下行未上传文件：${invalidFiles.map(n => parseInt(n) + 1).join(', ')}`);
            return;
        }

        // 构建FormData
        const formData = new FormData();
        const mainData = {
            applicant: getSafeValue('#applicant', ''),
            department: getSafeValue('#department', ''),
            // applyDate: new Date(getSafeValue('#date')).toISOString(),
            applyDate: new Date().toISOString(),
            engineer: getSafeValue('#engineer', ''),
            productCode: getSafeValue('#partNumber', ''),
            productName: getSafeValue('#partName', ''),
            specification: getSafeValue('#specification', ''),
            modelName: getSafeValue('#machineName', ''),
            plateThickness: getSafeValue('#thickness', '0'),
            plateMaterial: getSafeValue('#material', ''),
            versionType: getSafeValue('#versionType') === '升版' ? 1 : 0,
            cadFileName: `${getSafeValue('#partNumber', '')}_${getSafeValue('#machineName', '')}_` + new Date().toISOString()
                    .replace(/[:.]/g, '-')
                    .split('T')[0] + '_' +
                new Date().toTimeString().split(' ')[0].replace(/:/g, '-')
        };

        // 主数据
        const mainBlob = new Blob([JSON.stringify(mainData)], {type: 'application/json'});
        formData.append("main", mainBlob, "main.json");

        // 详情数据
        const details = [];
        const ardFiles = [];
        let hasVersionUpdate = false;
        programRecords.querySelectorAll('tr').forEach((row, index) => {
            const versionDisplay = row.querySelector('.displayVersionPrefix')?.textContent.trim();
            const version = parseVersion(versionDisplay);
            const {prefix, major, minor} = version;

            // 检查前缀是否在 maxVersions 中
            if (maxVersions[prefix]) {
                const max = maxVersions[prefix];
                // 比较版本号
                if (major < max.major || (major === max.major && minor <= max.minor)) {
                    // versionErrors.push(`第 ${index + 1} 行版本号 ${versionDisplay} 必须大于 ${prefix}${max.major}.${max.minor}`);
                } else {
                    hasVersionUpdate = true; // 标记存在版本更新
                }
            } else {
                hasVersionUpdate = true; // 标记存在版本更新
            }
            const detail = {
                scatteredType: row.querySelector('select.scattered-type')?.value || '前板',
                version: row.querySelector('.displayVersionPrefix')?.textContent || '',
                cncArdProgram: row.querySelector('.displayProgramName')?.textContent || '',
                remark: row.querySelector('td:last-child input')?.value || ''
            };
            details.push(detail);

            const file = row.querySelector('.file-upload-input')?.files[0];
            if (file) {
                const fileExtension = file.name.slice((Math.max(0, file.name.lastIndexOf(".")) || Infinity) + 1);
                const newFileName = `${detail.cncArdProgram}.${fileExtension}`;
                const newFile = new File([file], newFileName, {type: file.type});
                ardFiles.push(newFile);
            }
        });

        // 添加文件
        const detailsBlob = new Blob([JSON.stringify(details)], {type: 'application/json'});
        formData.append("details", detailsBlob, "details.json");
        ardFiles.forEach(file => formData.append("ardFiles", file));
        Array.from(document.getElementById('cadFiles')?.files || []).forEach(file => {
            formData.append("cadFiles", file, file.name);
        });
        const cadFiles = document.getElementById('cadFiles').files;
        if (hasVersionUpdate && cadFiles.length === 0) {
            alert("检测到版本更新，必须上传 CAD 文件！");
            return;
        }
        // 发送请求
        fetch(ip + '/cnc', {
            method: 'POST',
            body: formData,
            headers: {"X-Requested-With": "XMLHttpRequest"}
        })
            .then(response => response.text().then(text => ({
                ok: response.ok,
                status: response.status,
                text: text
            })))
            .then(({ok, status, text}) => {
                if (!ok) throw new Error(text || `请求失败 (${status})`);
                try {
                    return JSON.parse(text);
                } catch {
                    return text;
                }
            })
            .then(data => {
                alert('提交成功！');
                window.location.reload();
            })
            .catch(error => {
                alert(`提交失败：${error.message}`);
            });
    });

    // // 表单提交
    // document.getElementById('uploadForm').addEventListener('submit', function (e) {
    //     e.preventDefault();

    //     // 验证文件
    //     const invalidFiles = Array.from(document.querySelectorAll('.file-upload-input'))
    //         .filter(input => !input.files?.length)
    //         .map(input => input.dataset.fileId.replace('file', ''));

    //     if (invalidFiles.length > 0) {
    //         alert(`以下行未上传文件：${invalidFiles.map(n => parseInt(n) + 1).join(', ')}`);
    //         return;
    //     }

    //     // 构建FormData
    //     const formData = new FormData();
    //     const mainData = {
    //         applicant: getSafeValue('#applicant', ''),
    //         department: getSafeValue('#department', ''),
    //         applyDate: new Date(getSafeValue('#date')).toISOString(),
    //         engineer: getSafeValue('#engineer', ''),
    //         productCode: getSafeValue('#partNumber', ''),
    //         productName: getSafeValue('#partName', ''),
    //         specification: getSafeValue('#specification', ''),
    //         modelName: getSafeValue('#machineName', ''),
    //         plateThickness: getSafeValue('#thickness', '0'),
    //         plateMaterial: getSafeValue('#material', ''),
    //         versionType: getSafeValue('#versionType') === '升版' ? 1 : 0,
    //         cadFileName: `${getSafeValue('#partNumber', '')}_${getSafeValue('#machineName', '')}_` + new Date().toISOString()
    //             .replace(/[:.]/g, '-')
    //             .split('T')[0] + '_' +
    //             new Date().toTimeString().split(' ')[0].replace(/:/g, '-')
    //     };

    //     // 主数据
    //     const mainBlob = new Blob([JSON.stringify(mainData)], { type: 'application/json' });
    //     formData.append("main", mainBlob, "main.json");

    //     // 详情数据
    //     const details = [];
    //     const ardFiles = [];
    //     programRecords.querySelectorAll('tr').forEach((row, index) => {
    //         const detail = {
    //             scatteredType: row.querySelector('select.scattered-type')?.value || '前板',
    //             version: row.querySelector('.displayVersionPrefix')?.textContent || '',
    //             cncArdProgram: row.querySelector('.displayProgramName')?.textContent || '',
    //             remark: row.querySelector('td:last-child input')?.value || ''
    //         };
    //         details.push(detail);

    //         const file = row.querySelector('.file-upload-input')?.files[0];
    //         if (file) {
    //             const fileExtension = file.name.slice((Math.max(0, file.name.lastIndexOf(".")) || Infinity) + 1);
    //             const newFileName = `${detail.cncArdProgram}.${fileExtension}`;
    //             const newFile = new File([file], newFileName, { type: file.type });
    //             ardFiles.push(newFile);
    //         }
    //     });

    //     // 添加文件
    //     const detailsBlob = new Blob([JSON.stringify(details)], { type: 'application/json' });
    //     formData.append("details", detailsBlob, "details.json");
    //     ardFiles.forEach(file => formData.append("ardFiles", file));
    //     Array.from(document.getElementById('cadFiles')?.files || []).forEach(file => {
    //         formData.append("cadFiles", file, file.name);
    //     });

    //     // 发送请求
    //     fetch(ip + '/cnc', {
    //         method: 'POST',
    //         body: formData,
    //         headers: { "X-Requested-With": "XMLHttpRequest" }
    //     })
    //         .then(response => response.text().then(text => ({
    //             ok: response.ok,
    //             status: response.status,
    //             text: text
    //         })))
    //         .then(({ ok, status, text }) => {
    //             if (!ok) throw new Error(text || `请求失败 (${status})`);
    //             try {
    //                 return JSON.parse(text);
    //             } catch {
    //                 return text;
    //             }
    //         })
    //         .then(data => {
    //             alert('提交成功！');
    //             window.location.reload();
    //         })
    //         .catch(error => {
    //             alert(`提交失败：${error.message}`);
    //         });
    // });

    // 获取散板类型选项
    async function fetchScatteredTypes(productCode, currentRow) {
        if (!currentRow || !productCode) return;

        const dropdown = currentRow.querySelector('select.scattered-type');
        if (!dropdown) return;

        // 显示加载状态
        dropdown.disabled = true;
        dropdown.innerHTML = '<option value="">加载中...</option>';

        try {
            const response = await fetch(`${ip}/detail`);
            let types = await response.json();
            if (!Array.isArray(types)) types = [types]; // 兼容单对象

            populateScatteredTypeDropdown(types, currentRow);
        } catch (error) {
            console.error('获取散板类型失败:', error);
            dropdown.innerHTML = '<option value="">加载失败</option>';
        } finally {
            dropdown.disabled = false;
        }
    }

    // 动态生成下拉选项
    function populateScatteredTypeDropdown(types, currentRow) {
        if (!currentRow) return;
        const dropdown = currentRow.querySelector('select.scattered-type');
        if (!dropdown) return;

        dropdown.innerHTML = types.map(type =>
            `<option value="${type.scatteredType}">${type.scatteredType}</option>`
        ).join('');

        // 默认选中第一个选项
        if (types.length > 0) {
            dropdown.value = types[0].scatteredType;
            updateRowData(currentRow); // 触发版本号更新
        }
    }

    //新文件名校验
    document.body.addEventListener('change', function (event) {
        const target = event.target;
        if (target.classList.contains('file-upload-input')) {
            if (!validateProgramFile(target)) {
                return;
            }
            const displayElement = target.previousElementSibling;
            if (displayElement) {
                if (target.files.length === 0) {
                    displayElement.textContent = '未选择文件';
                    return;
                }
                const fileName = target.files[0].name;
                const maxLength = 12;
                displayElement.textContent = fileName.length > maxLength
                    ? fileName.substring(0, maxLength) + '...'
                    : fileName;
            }
        }
    });
    // 修改版本类型监听（在DOMContentLoaded内）
    document.querySelector('#versionType').addEventListener('change', () => {
        programRecords.querySelectorAll('tr').forEach(updateRowData);
    });
    // 版本类型变化监听
    // document.querySelector('#versionType').addEventListener('change', function () {
    //     // 动态切换CAD文件必填状态
    //     document.getElementById('cadFiles').required = (this.value === '升版');
    // });
    document.querySelector('#versionPrefix').addEventListener('change', () => {
        programRecords.querySelectorAll('tr').forEach(updateRowData);
    });
    document.getElementById('partNumber').addEventListener('input', function () {
        const productCode = this.value.trim();
        Array.from(programRecords.rows).forEach(row => {
            fetchScatteredTypes(productCode, row);
        });
        const historyTableBody = document.querySelector('#historyTable tbody');
        const columns = 11; // 总列数包含新增状态列

        // 防抖控制
        clearTimeout(this.searchTimeout);
        this.searchTimeout = setTimeout(async () => {
            try {
                // 并行请求
                const [categoryRes, historyRes] = await Promise.all([
                    fetch(`${ip}/category/${encodeURIComponent(productCode)}`),
                    fetch(`${ip}/cnc/${encodeURIComponent(productCode)}`)
                ]);
                // 处理历史记录
                if (!historyRes.ok) throw new Error(`历史记录请求失败: ${historyRes.status}`);
                historyData = await historyRes.json();
                maxVersions = getMaxVersionsByPrefix(historyData)
                programRecords.querySelectorAll('tr').forEach(updateRowData);
                renderHistoryTable(historyData, historyTableBody, columns);

                // 处理品类信息
                if (categoryRes.ok) {
                    const categoryData = await categoryRes.json();
                    handleCategoryInfo(categoryData);
                }


            } catch (error) {
                handleErrors(error, historyTableBody, columns);
            }
        }, 500);

        // 即时显示加载状态
        historyTableBody.innerHTML = `<tr><td colspan="${columns}">加载中...</td></tr>`;
        if (!productCode) {
            historyTableBody.innerHTML = `<tr><td colspan="${columns}">请输入品号查询</td></tr>`;
            resetCategoryFields();
            return;
        }
    });

    // 品类信息处理
    function handleCategoryInfo(data) {
        const partNameField = document.getElementById('partName');
        const specField = document.getElementById('specification');

        const isValidData = data?.categoryName && data?.specification;

        partNameField.value = isValidData ? data.categoryName : '';
        specField.value = isValidData ? data.specification : '';

        partNameField.readOnly = isValidData;
        specField.readOnly = isValidData;

        if (!isValidData) {
            console.warn('未获取到有效品类信息');
        }
    }

    // 历史表格渲染
    function renderHistoryTable(data, container, columns) {
        historyData = data;
        container.innerHTML = '';

        if (!data?.length) {
            container.innerHTML = `<tr><td colspan="${columns}">无相关历史记录</td></tr>`;
            return;
        }

        const fragment = document.createDocumentFragment();
        data.forEach(record => {
            record.details.forEach(detail => {
                const row = document.createElement('tr');

                // 字段配置（包含状态和挂起信息）
                const cells = [
                    record.productName,
                    record.productCode,
                    record.specification,
                    record.modelName,
                    detail.scatteredType,
                    record.applicant,
                    record.engineer,
                    formatDate(record.applyDate),
                    detail.version,
                    detail.cncArdProgram,
                    record.cadFileName,
                ];

                cells.forEach(value => {
                    const td = document.createElement('td');
                    td.innerHTML = value;
                    row.appendChild(td);
                });

                fragment.appendChild(row);
            });
        });

        container.appendChild(fragment);
    }

    // 错误处理
    function handleErrors(error, container, columns) {
        console.error('请求异常:', error);
        container.innerHTML = `
            <tr>
                <td colspan="${columns}" class="error">
                    数据加载失败: ${error.message || '未知错误'}
                </td>
            </tr>`;
        resetCategoryFields();
    }

    // 重置品类字段
    function resetCategoryFields() {
        document.getElementById('partName').value = '';
        document.getElementById('specification').value = '';
        document.getElementById('partName').readOnly = false;
        document.getElementById('specification').readOnly = false;
    }

    // 增强版日期格式化
    function formatDate(isoString) {
        try {
            if (!isoString) return '-';
            const date = new Date(isoString);
            return date.toLocaleDateString('zh-CN', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit'
            }).replace(/\//g, '-');
        } catch {
            return '-';
        }
    }
});