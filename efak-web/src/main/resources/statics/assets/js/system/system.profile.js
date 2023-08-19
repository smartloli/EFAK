
$("#efak_system_password_old").maxlength({
    warningClass: "badge mt-1 bg-success",
    limitReachedClass: "badge mt-1 bg-danger"
});

$("#efak_system_password_new").maxlength({
    warningClass: "badge mt-1 bg-success",
    limitReachedClass: "badge mt-1 bg-danger"
});

function alertNoti(msg, icon) {
    const Toast = Swal.mixin({
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 3000,
        timerProgressBar: true,
        didOpen: (toast) => {
            toast.addEventListener('mouseenter', Swal.stopTimer)
            toast.addEventListener('mouseleave', Swal.resumeTimer)
        }
    })

    // error or success
    Toast.fire({
        icon: icon,
        title: msg
    })
}

// cancle user password
$("#efak_system_user_password_cancle").click(function () {
    window.location.href = '/clusters/manage';
});

// reset user password
$("#efak_system_user_password_submit").click(function () {
    var passwordOld = $("#efak_system_password_old").val();
    var passwordNew = $("#efak_system_password_new").val();
    var username = $("#efak_system_profile_username").text();
    console.log(username)
    if (passwordOld.length == 0) {
        alertNoti("旧密码不能为空", "error");
        return;
    }
    if (passwordNew.length == 0) {
        alertNoti("新密码不能为空", "error");
        return;
    }

    $.ajax({
        url: '/system/user/password/reset',
        method: 'POST',
        data: {
            passwordOld: passwordOld,
            passwordNew: passwordNew,
            username: username
        },
        success: function (response) {
            Swal.fire({
                title: '成功',
                icon: 'success',
                html: '用户名[<code>' + username + '</code>]修改密码完成',
                allowOutsideClick: false
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = '/logout';
                }
            });
        },
        error: function (xhr, status, error) {
            Swal.fire('失败', '修改用户密码发生异常', 'error');
        }
    });
});