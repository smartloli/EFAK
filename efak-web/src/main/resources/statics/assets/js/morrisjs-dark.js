$(function() {
  'use strict';


  var colors = {
    primary        : "#6571ff",
    secondary      : "#7987a1",
    success        : "#05a34a",
    info           : "#66d1d1",
    warning        : "#fbbc06",
    danger         : "#ff3366",
    light          : "#e9ecef",
    dark           : "#060c17",
    muted          : "#7987a1",
    gridBorder     : "rgba(77, 138, 240, .15)",
    bodyColor      : "#b8c3d9",
    cardBg         : "#0c1427"
  }

  var fontFamily = "'Roboto', Helvetica, sans-serif"



  // Line Chart
  new Morris.Line({
    element: 'morrisLine',
    data: [
      { year: '2008', value: 2 },
      { year: '2009', value: 9 },
      { year: '2010', value: 5 },
      { year: '2011', value: 12 },
      { year: '2012', value: 5 }
    ],
    xkey: 'year',
    ykeys: ['value'],
    labels: ['value'],
    lineColors: [colors.danger],
    gridLineColor: [colors.gridBorder],
    gridTextColor: colors.bodyColor,
    gridTextFamily: fontFamily,
  });




  // Area Chart
  Morris.Area({
    element: 'morrisArea',
    data: [
      { y: '2006', a: 100, b: 90 },
      { y: '2007', a: 75,  b: 65 },
      { y: '2008', a: 50,  b: 40 },
      { y: '2009', a: 75,  b: 65 },
      { y: '2010', a: 50,  b: 40 },
      { y: '2011', a: 75,  b: 65 },
      { y: '2012', a: 100, b: 90 }
    ],
    xkey: 'y',
    ykeys: ['a', 'b'],
    labels: ['Series A', 'Series B'],
    lineColors: [colors.danger, colors.info],
    fillOpacity: 0.1,
    gridLineColor: [colors.gridBorder],
    gridTextColor: colors.bodyColor,
    gridTextFamily: fontFamily,
  });



  // Bar Chart
  Morris.Bar({
    element: 'morrisBar',
    data: [
      { y: '2006', a: 100, b: 90 },
      { y: '2007', a: 75,  b: 65 },
      { y: '2008', a: 50,  b: 40 },
      { y: '2009', a: 75,  b: 65 },
      { y: '2010', a: 50,  b: 40 },
      { y: '2011', a: 75,  b: 65 },
      { y: '2012', a: 100, b: 90 }
    ],
    xkey: 'y',
    ykeys: ['a', 'b'],
    labels: ['Series A', 'Series B'],
    barColors: [colors.danger, colors.info],
    gridLineColor: [colors.gridBorder],
    gridTextColor: colors.bodyColor,
    gridTextFamily: fontFamily,
  });



  // Donut Chart
  Morris.Donut({
    element: 'morrisDonut',
    data: [
      {label: "Download Sales", value: 12},
      {label: "In-Store Sales", value: 30},
      {label: "Mail-Order Sales", value: 20}
    ],
    colors: [colors.danger, colors.info, colors.primary],
    labelColor: colors.bodyColor,
  });

});