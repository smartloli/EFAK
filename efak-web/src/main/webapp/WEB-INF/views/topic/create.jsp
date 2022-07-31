<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="Create" name="loader"/>
    </jsp:include>

    <!-- Required common css -->
    <jsp:include page="../public/pro/css.jsp">
        <jsp:param value="plugins/notifications/lobibox.min.css" name="css"/>
    </jsp:include>
</head>

<body>


<!--start wrapper-->
<div class="wrapper">

    <!--start top header-->
    <jsp:include page="../public/pro/navtop.jsp"></jsp:include>
    <!--end top header-->

    <!--start sidebar -->
    <jsp:include page="../public/pro/navbar.jsp"></jsp:include>
    <!--end sidebar -->

    <!--start content-->
    <main class="page-content">
        <!--breadcrumb-->
        <div class="page-breadcrumb d-none d-sm-flex align-items-center mb-3">
            <div class="breadcrumb-title pe-3">Topics</div>
            <div class="ps-3">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb mb-0 p-0">
                        <li class="breadcrumb-item"><a href="/"><i class="bx bx-home-alt"></i></a>
                        </li>
                        <li class="breadcrumb-item active" aria-current="page">Create</li>
                    </ol>
                </nav>
            </div>
        </div>
        <!--end breadcrumb-->

        <div class="row">
            <div class="col-xl-6 mx-auto">

                <div class="card">
                    <div class="card-body">
                        <div class="border p-3 rounded">
                            <h6 class="mb-0 text-uppercase">Topic Property</h6>
                            <hr/>
                            <form class="row g-3" role="form" action="/topic/create/form" method="post"
                                  onsubmit="return contextFormValid();return false;">
                                <div class="col-12">
                                    <label class="form-label">Topic Name (*)</label>
                                    <input id="ke_topic_name" name="ke_topic_name" class="form-control"
                                           maxlength=50>
                                    <label for="inputError" class="control-label text-danger">
                                        <i class="bx bx-info-circle"></i> Made up of letters and digits or
                                        underscores . Such as "efak_topic_001" .
                                    </label>
                                </div>
                                <div class="col-12">
                                    <label class="form-label">Partitions (*)</label>
                                    <input id="ke_topic_partition" name="ke_topic_partition"
                                           class="form-control" maxlength=50 value="1">
                                    <label for="inputError" class="control-label text-danger">
                                        <i class="bx bx-info-circle"></i> Partition parameters must be
                                        numeric .
                                    </label>
                                </div>
                                <div class="col-12">
                                    <label class="form-label">Replication Factor (*)</label>
                                    <input id="ke_topic_repli" name="ke_topic_repli" class="form-control"
                                           maxlength=50 value="1">
                                    <label for="inputError" class="control-label text-danger">
                                        <i class="bx bx-info-circle"></i> Replication Factor parameters must
                                        be numeric . Pay attention to available brokers must be larger than
                                        replication factor .
                                    </label>
                                </div>
                                <div class="col-12">
                                    <div class="d-grid">
                                        <button type="submit" class="btn btn-primary">Submit</button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!--end row-->
    </main>
    <!--end page main-->


    <!--start overlay-->
    <div class="overlay nav-toggle-icon"></div>
    <!--end overlay-->

    <!--Start Back To Top Button-->
    <a href="javaScript:;" class="back-to-top"><i class='bx bxs-up-arrow-alt'></i></a>
    <!--End Back To Top Button-->

</div>
<!--end wrapper-->

<!-- import js and plugins -->
<jsp:include page="../public/pro/script.jsp">
    <jsp:param value="plugins/notifications/lobibox.min.js" name="loader"/>
    <jsp:param value="plugins/notifications/notifications.min.js" name="loader"/>
    <jsp:param value="main/topic/create.js?v=3.0.0" name="loader"/>
</jsp:include>
</body>
<script type="text/javascript">
    function contextFormValid() {
        var ke_topic_name = $("#ke_topic_name").val();
        var ke_topic_partition = $("#ke_topic_partition").val();
        var ke_topic_repli = $("#ke_topic_repli").val();
        var reg = /^[A-Za-z0-9_-]+$/;
        var digit = /^[0-9]+$/;
        if (ke_topic_name.length == 0 || !reg.test(ke_topic_name)) {
            error_noti("Topic name input does not conform to naming conventions.")
            return false;
        }
        if (ke_topic_partition.length == 0 || !digit.test(ke_topic_partition)) {
            error_noti("Topic partitions is not null or must be number.")
            return false;
        }
        if (ke_topic_repli.length == 0 || !digit.test(ke_topic_repli)) {
            error_noti("Topic replication is not null or must be number.")
            return false;
        }
        return true;
    }

    function error_noti(errorMsg) {
        console.log(errorMsg)
        Lobibox.notify('error', {
            pauseDelayOnHover: true,
            continueDelayOnInactiveTab: false,
            position: 'top right',
            icon: 'bx bx-x-circle',
            msg: errorMsg
        });
    }
</script>
</html>
