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
    bodyColor      : "#000",
    cardBg         : "#fff"
  }

  var fontFamily = "'Roboto', Helvetica, sans-serif"



  //Line Chart 
  $.plot($('#flotLine'), [
    {
      label: 'Visits',
      data: [
        [ 6, 196 ], [ 7, 175 ], [ 8, 212 ], [ 9, 247 ], [ 10, 152 ], [ 11, 225 ], [ 12, 155 ], [ 13, 203 ], [ 14, 166 ], [ 15, 151 ]
      ]
    },
    {
      label: 'Returning visits',
      data: [
        [ 6, 49 ], [ 7, 56 ], [ 8, 30 ], [ 9, 29 ], [ 10, 66 ], [ 11, 2 ], [ 12, 2 ], [ 13, 8 ], [ 14, 34 ], [ 15, 63 ]
      ]
    }
  ], {
    series: {
      shadowSize: 0,
      lines: {
        show: true
      },
      points: {
        show: true,
        radius: 4
      }
    },

    grid: {
      color: colors.bodyColor,
      borderColor: colors.gridBorder,
      borderWidth: 1,
      hoverable: true,
      clickable: true
    },

    xaxis: { tickColor: colors.gridBorder, },
    yaxis: { tickColor: colors.gridBorder, },
    legend: { backgroundColor: colors.cardBg },
    tooltip: { show: true },
    colors: [colors.danger, colors.primary]
  });




  // Bar Chart
  $.plot($('#flotBar'), [
    {
      label: 'Visits',
      data: [
        [ 6, 156 ], [ 7, 195 ], [ 8, 171 ], [ 9, 211 ], [ 10, 150 ], [ 11, 169 ], [ 12, 173 ], [ 13, 200 ], [ 14, 233 ], [ 15, 161 ]
      ]
    },
    {
      label: 'Returning visits',
      data: [
        [ 6, 24 ], [ 7, 20 ], [ 8, 31 ], [ 9, 4 ], [ 10, 92 ], [ 11, 87 ], [ 12, 28 ], [ 13, 21 ], [ 14, 80 ], [ 15, 76 ]
      ]
    }
  ], {
    series: {
      shadowSize: 0,
      bars: {
        show: true,
        barWidth: .6,
        align: 'center',
        lineWidth: 1,
        fill: 0.25
      }
    },

    grid: {
      color: colors.bodyColor,
      borderColor: colors.gridBorder,
      borderWidth: 1,
      hoverable: true,
      clickable: true
    },

    xaxis: { tickDecimals: 2, tickColor: colors.gridBorder },
    yaxis: { tickColor: colors.gridBorder },
    legend: { backgroundColor: colors.cardBg },

    tooltip: { show: true },
    colors: [colors.danger, colors.primary]
  });




  // Area Chart
  $.plot($('#flotArea'), [
    {
      label: 'iPhone',
      data: [
        [ "2010.Q1", 35 ], [ '2010.Q2', 67 ], [ '2010.Q3', 13 ], [ '2010.Q4', 45 ]
      ]
    },
    {
      label: 'iTouch',
      data: [
        [ '2010.Q1', 32 ], [ '2010.Q2', 49 ], [ '2010.Q3', 25 ], [ '2010.Q4', 57 ]
      ]
    }
  ], {
    series: {
      shadowSize: 0,
      lines: {
        show: true,
        fill: 0.15,
        lineWidth: 1
      }
    },

    grid: {
      color: colors.bodyColor,
      borderColor: colors.gridBorder,
      borderWidth: 1,
      hoverable: true,
      clickable: true
    },

    xaxis: { mode: 'categories', tickColor: colors.gridBorder },
    yaxis: { tickColor: colors.gridBorder },
    legend: { backgroundColor: colors.cardBg },

    tooltip: {
      show: true,
      content: '%s: %y'
    },

    colors: [colors.danger, colors.primary]
  });




  // Pie Chart
  $.plot($('#flotPie'), [
    { label: 'Series1', data: 77 },
    { label: 'Series2', data: 81 },
    { label: 'Series3', data: 46 },
    { label: 'Series4', data: 35 },
    { label: 'Series5', data: 79 },
    { label: 'Series6', data: 84 },
  ], {
    series: {
      shadowSize: 0,
      pie: {
        show: true,
        radius: 1,
        innerRadius: 0.5,
        stroke: {
          color: colors.cardBg,
          width: 3
        },
        label: {
          show: true,
          radius: 3 / 4,
          background: { opacity: 0.5 },

          formatter: function(label, series) {
            return '<div style="font-size:11px;text-align:center;color:white;">' + Math.round(series.percent) + '%</div>';
          }
        }
      }
    },

    grid: {
      color: colors.bodyColor,
      borderColor: colors.gridBorder,
      borderWidth: 1,
      hoverable: true,
      clickable: true
    },

    xaxis: { tickColor: colors.gridBorder },
    yaxis: { tickColor: colors.gridBorder },
    legend: { backgroundColor: colors.cardBg },
    colors: [colors.primary, colors.secondary, colors.danger, colors.warning, colors.info, colors.success]
  });





  //  Real-Time Chart
  var data = [],
    totalPoints = 300;

  function getRandomData() {

    if (data.length > 0)
      data = data.slice(1);

    // Do a random walk

    while (data.length < totalPoints) {

      var prev = data.length > 0 ? data[data.length - 1] : 50,
        y = prev + Math.random() * 10 - 5;

      if (y < 0) {
        y = 0;
      } else if (y > 100) {
        y = 100;
      }

      data.push(y);
    }

    // Zip the generated y values with the x values

    var res = [];
    for (var i = 0; i < data.length; ++i) {
      res.push([i, data[i]])
    }

    return res;
  }

  // Set up the control widget

  var updateInterval = 30;
  if ($("#flotRealTime").length) {
    var plot = $.plot("#flotRealTime", [getRandomData()], {
      series: {
        shadowSize: 0, // Drawing is faster without shadows
        lines: {
          show: true,
          lineWidth: 1,
          fill: false,
          opacity: 0.1
        }
      },
      xaxis: {
        // show: false,
      },
      yaxis: {
        min: 0,
        max: 150
      },
      grid: {
        color: 'rgba(77, 138, 240, 1)',
        borderColor: colors.gridBorder,
        borderWidth: 1,
        hoverable: true,
        clickable: true
      },
      colors: [colors.primary]

    });

    function update() {

      plot.setData([getRandomData()]);

      // Since the axes don't change, we don't need to call plot.setupGrid()

      plot.draw();
      setTimeout(update, updateInterval);
    }

    update();
  }

});
