$(function() {
  'use strict';


  // Simple list example
  if ($("#simple-list").length) {
    var simpleList = document.querySelector("#simple-list");
    new Sortable(simpleList, {
      animation: 150,
      ghostClass: 'bg-dark'
    });
  }



  // Handle example
  if ($("#handle-example").length) {
    var handleExample = document.querySelector("#handle-example");
    new Sortable(handleExample, {
      handle: '.handle', // handle's class
      animation: 150,
      ghostClass: 'bg-dark'
    });
  }



  // Shared lists example
  if ($("#shared-list-left").length) {
    var sharedListLeft = document.querySelector("#shared-list-left");
    new Sortable(sharedListLeft, {
      group: 'shared', // set both lists to same group
      animation: 150,
      ghostClass: 'bg-dark'
    });
  }
  if ($("#shared-list-right").length) {
    var sharedListRight = document.querySelector("#shared-list-right");
    new Sortable(sharedListRight, {
      group: 'shared', // set both lists to same group
      animation: 150,
      ghostClass: 'bg-dark'
    });
  }



  // Cloning example
  if ($("#shared-list-2-left").length) {
    var sharedList2Left = document.querySelector("#shared-list-2-left");
    new Sortable(sharedList2Left, {
      group: {
        name: 'shared2',
        pull: 'clone' // To clone: set pull to 'clone'
      },
      animation: 150,
      ghostClass: 'bg-dark'
    });
  }
  if ($("#shared-list-2-right").length) {
    var sharedList2Right = document.querySelector("#shared-list-2-right");
    new Sortable(sharedList2Right, {
      group: {
        name: 'shared2',
        pull: 'clone' // To clone: set pull to 'clone'
      },
      animation: 150,
      ghostClass: 'bg-dark'
    });
  }



  // Disabling sorting example
  if ($("#shared-list-3-left").length) {
    var sharedList3Left = document.querySelector("#shared-list-3-left");
    new Sortable(sharedList3Left, {
      group: {
        name: 'shared3',
        pull: 'clone',
        put: false // Do not allow items to be put into this list
      },
      animation: 150,
      ghostClass: 'bg-dark',
      sort: false // To disable sorting: set sort to false
    });
  }
  if ($("#shared-list-3-right").length) {
    var sharedList3Right = document.querySelector("#shared-list-3-right");
    new Sortable(sharedList3Right, {
      group: {
        name: 'shared3',
      },
      animation: 150,
      ghostClass: 'bg-dark'
    });
  }


  
  // Filter example
  if ($("#filter-example").length) {
    var filterExample = document.querySelector("#filter-example");
    new Sortable(filterExample, {
      filter: '.filtered', // 'filtered' class is not draggable
      animation: 150,
      ghostClass: 'bg-dark'
    });
  }



  // Grid example
  if ($("#grid-example").length) {
    var gridExample = document.querySelector("#grid-example");
    new Sortable(gridExample, {
      animation: 150,
      ghostClass: 'bg-dark'
    });
  }



  // Nested example
  if ($("#nested-sortable").length) {
    var nestedSortables = [].slice.call(document.querySelectorAll('.nested-sortable'));

    // Loop through each nested sortable element
    for (var i = 0; i < nestedSortables.length; i++) {
      new Sortable(nestedSortables[i], {
        group: 'nested',
        animation: 150,
        fallbackOnBody: true,
        swapThreshold: 0.65
      });
    }
  }


});