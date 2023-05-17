$(function() {
  'use strict';

  if ($('#ace_html').length) {
    $(function() {
      var editor = ace.edit("ace_html");
      editor.setTheme("ace/theme/dracula");
      editor.getSession().setMode("ace/mode/html");
      editor.setOption("showPrintMargin", false)
    });
  }
  if ($('#ace_scss').length) {
    $(function() {
      var editor = ace.edit("ace_scss");
      editor.setTheme("ace/theme/dracula");
      editor.getSession().setMode("ace/mode/scss");
      editor.setOption("showPrintMargin", false)
    });
  }
  if ($('#ace_javaScript').length) {
    $(function() {
      var editor = ace.edit("ace_javaScript");
      editor.setTheme("ace/theme/dracula");
      editor.getSession().setMode("ace/mode/javascript");
      editor.setOption("showPrintMargin", false)
    });
  }

});