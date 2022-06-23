<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="Metadata" name="loader"/>
    </jsp:include>

    <!-- Required common css -->
    <jsp:include page="../public/pro/css.jsp">
        <jsp:param value="plugins/notifications/lobibox.min.css" name="css"/>
        <jsp:param value="plugins/select2/select2.min.css" name="css"/>
        <jsp:param value="plugins/select2/select2-bootstrap4.css" name="css"/>
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
                        <li class="breadcrumb-item active" aria-current="page">Metadata</li>
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
                            <h6 class="mb-0 text-uppercase">Config Topic Property</h6>
                            <hr/>
                            <div class="col-12">
                                <label class="form-label">Topic Type (*)</label>
                                <br/>
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input" type="radio" name="ke_topic_alter"
                                           id="ke_topic_alter" value="add_config" checked="">
                                    <label class="form-check-label" for="ke_topic_alter">Add Config</label>
                                </div>
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input" type="radio" name="ke_topic_alter"
                                           id="ke_topic_delete" value="del_config">
                                    <label class="form-check-label" for="ke_topic_delete">Delete Config</label>
                                </div>
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input" type="radio" name="ke_topic_alter"
                                           id="ke_topic_describe" value="desc_config">
                                    <label class="form-check-label" for="ke_topic_describe">Describe Config</label>
                                </div>
                                <br/>
                                <label for="inputError" class="control-label text-danger">
                                    <i class="bx bx-info-circle"></i> Select operate type when you getter/setter
                                    topic .
                                </label>
                            </div>
                            <hr/>
                            <div class="col-12">
                                <label class="form-label">Topic Name (*)</label>
                                <br/>
                                <select id="select2val" name="select2val" tabindex="-1"
                                        style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
                                <input id="ke_topic_name" name="ke_topic_name" type="hidden"/>
                                <label for="inputError" class="control-label text-danger">
                                    <i class="bx bx-info-circle"></i> Choice the topic you need to alter .
                                </label>
                            </div>
                            <hr/>
                            <div id="div_topic_keys" class="col-12">
                                <label class="form-label">Topic Property Key (*)</label>
                                <br/>
                                <select id="select2key" name="select2key" tabindex="-1"
                                        style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
                                <input id="ke_topic_key" name="ke_topic_key" type="hidden"/>
                                <label for="inputError" class="control-label text-danger">
                                    <i class="bx bx-info-circle"></i> Choice the topic property key you need to set .
                                </label>
                            </div>
                            <div id="efak_topic_metadata_key_hr">
                                <hr/>
                            </div>
                            <div id="div_topic_value" class="col-12">
                                <label class="form-label">Topic Property Value (*)</label>
                                <br/>
                                <input id="ke_topic_value" name="ke_topic_value" class="form-control"
                                       maxlength=50>
                                <label for="inputError" class="control-label text-danger">
                                    <i class="bx bx-info-circle"></i> Set the topic property value when you submit
                                    setter .
                                </label>
                            </div>
                            <div id="efak_topic_metadata_value_hr">
                                <hr/>
                            </div>
                            <div id="div_topic_msg" class="col-12">
                                <label class="form-label">Message (*)</label>
                                <textarea id="ke_topic_config_content" name="ke_topic_config_content"
                                          class="form-control"
                                          placeholder="" rows="5" readonly></textarea>
                                <label for="inputError" class="control-label text-danger">
                                    <i class="bx bx-info-circle"></i> Get result from server when you getter/setter
                                    topic .
                                </label>
                            </div>
                            <div class="col-12">
                                <div class="d-grid">
                                    <button id="btn_send" type="button" class="btn btn-primary">Submit</button>
                                </div>
                            </div>
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
    <jsp:param value="plugins/select2/select2.min.js" name="loader"/>
    <jsp:param value="main/topic/manager.js?v=3.0.0" name="loader"/>
</jsp:include>
</body>
</html>
