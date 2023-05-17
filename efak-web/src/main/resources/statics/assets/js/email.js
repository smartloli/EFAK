$(function() {
  'use strict'

  if ($(".compose-multiple-select").length) {
    $(".compose-multiple-select").select2();
  }

  /*simplemde editor*/
  if ($("#simpleMdeEditor").length) {
    var simplemde = new SimpleMDE({
      element: $("#simpleMdeEditor")[0]
    });
  }

});