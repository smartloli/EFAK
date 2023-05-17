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



  // Apex Line chart start
  if ($('#apexLine').length) {
    var lineChartOptions = {
      chart: {
        type: "line",
        height: '320',
        parentHeightOffset: 0,
        foreColor: colors.bodyColor,
        background: colors.cardBg,
        toolbar: {
          show: false
        },
      },
      theme: {
        mode: 'dark'
      },
      tooltip: {
        theme: 'dark'
      },
      colors: [colors.primary, colors.danger, colors.warning],
      grid: {
        padding: {
          bottom: -4
        },
        borderColor: colors.gridBorder,
        xaxis: {
          lines: {
            show: true
          }
        }
      },
      series: [
        {
          name: "Data a",
          data: [45, 52, 38, 45]
        },
        {
          name: "Data b",
          data: [12, 42, 68, 33]
        },
        {
          name:
            "Data c",
          data: [8, 32, 48, 53]
        }
      ],
      xaxis: {
        type: "datetime",
        categories: ["2015", "2016", "2017", "2018"],
        lines: {
          show: true
        },
        axisBorder: {
          color: colors.gridBorder,
        },
        axisTicks: {
          color: colors.gridBorder,
        },
      },
      markers: {
        size: 0,
      },
      legend: {
        show: true,
        position: "top",
        horizontalAlign: 'center',
        fontFamily: fontFamily,
        itemMargin: {
          horizontal: 8,
          vertical: 0
        },
      },
      stroke: {
        width: 3,
        curve: "smooth",
        lineCap: "round"
      },
    };
    var apexLineChart = new ApexCharts(document.querySelector("#apexLine"), lineChartOptions);
    apexLineChart.render();
  }
  // Apex Line chart end



  // Apex Bar chart start
  if ($('#apexBar').length) {
    var options = {
      chart: {
        type: 'bar',
        height: '320',
        parentHeightOffset: 0,
        foreColor: colors.bodyColor,
        background: colors.cardBg,
        toolbar: {
          show: false
        },
      },
      theme: {
        mode: 'dark'
      },
      tooltip: {
        theme: 'dark'
      },
      colors: [colors.primary],    
      grid: {
        padding: {
          bottom: -4
        },
        borderColor: colors.gridBorder,
        xaxis: {
          lines: {
            show: true
          }
        }
      },
      series: [{
        name: 'sales',
        data: [30,40,45,50,49,60,70,91,125]
      }],
      xaxis: {
        type: 'datetime',
        categories: ['01/01/1991','01/01/1992','01/01/1993','01/01/1994','01/01/1995','01/01/1996','01/01/1997', '01/01/1998','01/01/1999'],
        axisBorder: {
          color: colors.gridBorder,
        },
        axisTicks: {
          color: colors.gridBorder,
        },
      },
      legend: {
        show: true,
        position: "top",
        horizontalAlign: 'center',
        fontFamily: fontFamily,
        itemMargin: {
          horizontal: 8,
          vertical: 0
        },
      },
      stroke: {
        width: 0
      },
      plotOptions: {
        bar: {
          borderRadius: 4
        }
      }
    }
    
    var apexBarChart = new ApexCharts(document.querySelector("#apexBar"), options);
    apexBarChart.render();
  }
  // Apex Bar chart end




  // Apex Area chart start
  if ($('#apexArea').length) {
    var options = {
      chart: {
        type: "area",
        height: 300,
        parentHeightOffset: 0,
        foreColor: colors.bodyColor,
        background: colors.cardBg,
        toolbar: {
          show: false
        },
        stacked: true,
      },
      theme: {
        mode: 'dark'
      },
      tooltip: {
        theme: 'dark'
      },
      colors: [colors.danger, colors.info],
      stroke: {
        curve: "smooth",
        width: 3
      },
      dataLabels: {
        enabled: false
      },
      series: [{
        name: 'Total Views',
        data: generateDayWiseTimeSeries(0, 18)
      }, {
        name: 'Unique Views',
        data: generateDayWiseTimeSeries(1, 18)
      }],
      // markers: {
      //   size: 5,
      //   strokeWidth: 3,
      //   hover: {
      //     size: 7
      //   }
      // },
      xaxis: {
        type: "datetime",
        axisBorder: {
          color: colors.gridBorder,
        },
        axisTicks: {
          color: colors.gridBorder,
        },
      },
      yaxis: {
        title: {
          text: 'Views',
        },
        tickAmount: 4,
        min: 0,
        labels: {
          // offsetX: -6,
        },
        tooltip: {
          enabled: true
        }
      },
      grid: {
        padding: {
          bottom: -4
        },
        borderColor: colors.gridBorder,
        xaxis: {
          lines: {
            show: true
          }
        }
      },
      tooltip: {
        x: {
          format: "dd MMM yyyy"
        },
      },
      fill: {
        type: 'solid',
        opacity: [0.4,0.25],
      },
      legend: {
        show: true,
        position: "top",
        horizontalAlign: 'center',
        fontFamily: fontFamily,
        itemMargin: {
          horizontal: 8,
          vertical: 0
        },
      },
    };

    var chart = new ApexCharts(document.querySelector("#apexArea"), options);
    chart.render();

    function generateDayWiseTimeSeries(s, count) {
      var values = [
        [4,3,10,9,29,19,25,9,12,7,19,5,13,9,17,2,7,5],
        [2,3,8,7,22,16,23,7,11,5,12,5,10,4,15,2,6,2]
      ];
      var i = 0;
      var series = [];
      var x = new Date("11 Nov 2012").getTime();
      while (i < count) {
        series.push([x, values[s][i]]);
        x += 86400000;
        i++;
      }
      return series;
    }
  }
  // Apex Area chart end




  // Apex Mixed chart start
  if ($('#apexMixed').length) {
    var options = {
      chart: {
        height: 300,
        type: 'line',
        stacked: false,
        parentHeightOffset: 0,
        foreColor: colors.bodyColor,
        background: colors.cardBg,
        toolbar: {
          show: false
        },
      },
      theme: {
        mode: 'dark'
      },
      tooltip: {
        theme: 'dark'
      },
      colors: [colors.danger, colors.info],
      grid: {
        borderColor: colors.gridBorder,
        padding: {
          bottom: -4
        },
        xaxis: {
          lines: {
            show: true
          }
        }
      },
      stroke: {
        width: [0, 3],
        curve: 'smooth'
      },
      plotOptions: {
        bar: {
          columnWidth: '50%'
        }
      },
      series: [{
        name: 'Team A',
        type: 'column',
        data: [23, 11, 22, 27, 13, 22, 37, 21, 44, 22, 30]
      }, {
        name: 'Team B',
        type: 'area',
        data: [44, 55, 41, 67, 22, 43, 21, 41, 56, 27, 43]
      }],
      legend: {
        show: true,
        position: "top",
        horizontalAlign: 'center',
        fontFamily: fontFamily,
        itemMargin: {
          horizontal: 8,
          vertical: 0
        },
      },
      fill: {
        opacity: [.75,0.25],
      },
      labels: ['01/01/2003', '02/01/2003','03/01/2003','04/01/2003','05/01/2003','06/01/2003','07/01/2003','08/01/2003','09/01/2003','10/01/2003','11/01/2003'],
      markers: {
        size: 0
      },
      xaxis: {
        type:'datetime',
        axisBorder: {
          color: colors.gridBorder,
        },
        axisTicks: {
          color: colors.gridBorder,
        },
      },
      yaxis: {
        title: {
          text: 'Points',
        },
      },
      tooltip: {
        shared: true,
        intersect: false,
        y: [{
          formatter: function (y) {
            if(typeof y !== "undefined") {
              return  y.toFixed(0) + " points";
            }
            return y;
          }
        }, {
          formatter: function (y) {
            if(typeof y !== "undefined") {
              return  y.toFixed(2) + " $";
            }
            return y;
          }
        }]
      }
    }
    var chart = new ApexCharts(
      document.querySelector("#apexMixed"),
      options
    );
    chart.render();
  }
  // Apex Mixed chart end




  // Apex Donut chart start
  if ($('#apexDonut').length) {
    var options = {
      chart: {
        height: 300,
        type: "donut",
        foreColor: colors.bodyColor,
        background: colors.cardBg,
        toolbar: {
          show: false
        },
      },
      theme: {
        mode: 'dark'
      },
      tooltip: {
        theme: 'dark'
      },
      stroke: {
        colors: ['rgba(0,0,0,0)']
      },
      colors: [colors.primary,colors.warning,colors.danger, colors.info],
      legend: {
        show: true,
        position: "top",
        horizontalAlign: 'center',
        fontFamily: fontFamily,
        itemMargin: {
          horizontal: 8,
          vertical: 0
        },
      },
      dataLabels: {
        enabled: false
      },
      series: [44, 55, 13, 33]
    };

    var chart = new ApexCharts(document.querySelector("#apexDonut"), options);
    chart.render();
  }
  // Apex Donut chart start



  
  // Apex Pie chart end
  if ($('#apexPie').length) {
    var options = {
      chart: {
        height: 300,
        type: "pie",
        foreColor: colors.bodyColor,
        background: colors.cardBg,
        toolbar: {
          show: false
        },
      },
      theme: {
        mode: 'dark'
      },
      tooltip: {
        theme: 'dark'
      },
      colors: [colors.primary,colors.warning,colors.danger, colors.info],
      legend: {
        show: true,
        position: "top",
        horizontalAlign: 'center',
        fontFamily: fontFamily,
        itemMargin: {
          horizontal: 8,
          vertical: 0
        },
      },
      stroke: {
        colors: ['rgba(0,0,0,0)']
      },
      dataLabels: {
        enabled: false
      },
      series: [44, 55, 13, 33]
    };
    
    var chart = new ApexCharts(document.querySelector("#apexPie"), options);
    chart.render();  
  }
  // Apex Pie chart end




  // Apex Heat chart start
  if ($('#apexHeatMap').length) {
    function generateData(count, yrange) {
      var i = 0;
      var series = [];
      while (i < count) {
          var x = 'w' + (i + 1).toString();
          var y = Math.floor(Math.random() * (yrange.max - yrange.min + 1)) + yrange.min;

          series.push({
              x: x,
              y: y
          });
          i++;
      }
      return series;
  }

  var options = {
      chart: {
        height: 300,
        type: 'heatmap',
        parentHeightOffset: 0,
        foreColor: colors.bodyColor,
        background: colors.cardBg,
        toolbar: {
          show: false
        },
      },
      theme: {
        mode: 'dark'
      },
      tooltip: {
        theme: 'dark'
      },
      grid: {
        borderColor: colors.gridBorder,
        padding: {
          bottom: -4
        },
        xaxis: {
          lines: {
            show: true
          }
        }
      },
      dataLabels: {
        enabled: false
      },
      colors: [colors.info],
      stroke: {
        colors: [colors.cardBg]
      },
      series: [{
          name: 'Metric1',
          data: generateData(18, {
            min: 0,
            max: 90
          })
        },
        {
          name: 'Metric2',
          data: generateData(18, {
            min: 0,
            max: 90
          })
        },
        {
          name: 'Metric3',
          data: generateData(18, {
            min: 0,
            max: 90
          })
        },
        {
          name: 'Metric4',
          data: generateData(18, {
            min: 0,
            max: 90
          })
        },
        {
          name: 'Metric5',
          data: generateData(18, {
            min: 0,
            max: 90
          })
        },
        {
          name: 'Metric6',
          data: generateData(18, {
            min: 0,
            max: 90
          })
        },
        {
          name: 'Metric7',
          data: generateData(18, {
            min: 0,
            max: 90
          })
        },
        {
          name: 'Metric8',
          data: generateData(18, {
            min: 0,
            max: 90
          })
        },
        {
          name: 'Metric9',
          data: generateData(18, {
            min: 0,
            max: 90
          })
        }
      ],
      xaxis: {
        axisBorder: {
          color: colors.gridBorder,
        },
        axisTicks: {
          color: colors.gridBorder,
        },
      },
      title: {
        text: 'HeatMap Chart (Single color)',
        align: 'center',
        style: {
          fontWeight:  'normal',
        },
      },
      plotOptions: {
        heatmap: {
          radius: 0,
        }
      },

    }

    var chart = new ApexCharts(document.querySelector("#apexHeatMap"), options);
    chart.render();
  }
  // Apex Heat chart end




  // Apex Radar chart start
  if ($('#apexRadar').length) {
    var options = {
      chart: {
        height: 300,
        type: 'radar',
        parentHeightOffset: 0,
        foreColor: colors.bodyColor,
        background: colors.cardBg,
        toolbar: {
          show: false
        },
      },
      theme: {
        mode: 'dark'
      },
      tooltip: {
        theme: 'dark'
      },
      grid: {
        padding: {
          bottom: -6
        }
      },
      legend: {
        show: true,
        position: "top",
        horizontalAlign: 'center',
        fontFamily: fontFamily,
        itemMargin: {
          horizontal: 8,
          vertical: 0
        },
      },
      series: [{
        name: 'Series 1',
        data: [80, 50, 30, 40, 100, 20],
      }, {
        name: 'Series 2',
        data: [20, 30, 40, 80, 20, 80],
      }, {
        name: 'Series 3',
        data: [44, 76, 78, 13, 43, 10],
      }],
      labels: ['2011', '2012', '2013', '2014', '2015', '2016'],
      colors: [colors.primary, colors.warning, colors.danger],
      stroke: {
        width: 0,
      },
      fill: {
        opacity: 0.75
      },
      xaxis: {
        categories: ['April', 'May', 'June', 'July', 'August', 'September'],
        labels: {
          show: true,
          style: {
            colors: [colors.secondary, colors.secondary, colors.secondary, colors.secondary, colors.secondary, colors.secondary],
            fontSize: "14px",
            fontFamily: fontFamily
          }
        }
      },
      yaxis: {
        labels: {
          show: true,
          style: {
            colors: colors.bodyColor,
            fontSize: "11px",
            fontFamily: fontFamily
          }
        }
      },
      markers: {
        size: 0
      },
      plotOptions: {
        radar: {
          polygons: {
            strokeColors: colors.gridBorder,
            strokeWidth: 1,
            connectorColors: colors.gridBorder,
            fill: {
                colors: ['transparent']
            }
          }
        }
      }
    }

    var chart = new ApexCharts(document.querySelector("#apexRadar"), options );
    chart.render();
  }
  // Apex Radar chart end



  
  // Apex Scatter chart start
  if ($('#apexScatter').length) {
    var options = {
      chart: {
        height: 300,
        type: 'scatter',
        parentHeightOffset: 0,
        foreColor: colors.bodyColor,
        background: colors.cardBg,
        toolbar: {
          show: false
        },
      },
      theme: {
        mode: 'dark'
      },
      tooltip: {
        theme: 'dark'
      },
      colors: [colors.primary, colors.warning, colors.danger],
      grid: {
        borderColor: colors.gridBorder,
        padding: {
          bottom: -4
        },
        xaxis: {
          lines: {
            show: true
          }
        }
      },
      markers: {
        strokeColor: colors.cardBg,
        hover: {
          strokeColor: colors.cardBg

        }
      },
      legend: {
        show: true,
        position: "top",
        horizontalAlign: 'center',
        fontFamily: fontFamily,
        itemMargin: {
          horizontal: 8,
          vertical: 0
        },
      },
      series: [{
        name: "Sample A",
        data: [
        [16.4, 5.4], [21.7, 2], [25.4, 3], [19, 2], [10.9, 1], [13.6, 3.2], [10.9, 7.4], [10.9, 0], [10.9, 8.2], [16.4, 0], [16.4, 1.8], [13.6, 0.3], [13.6, 0], [29.9, 0], [27.1, 2.3], [16.4, 0], [13.6, 3.7], [10.9, 5.2], [16.4, 6.5], [10.9, 0], [24.5, 7.1], [10.9, 0], [8.1, 4.7], [19, 0], [21.7, 1.8], [27.1, 0], [24.5, 0], [27.1, 0], [29.9, 1.5], [27.1, 0.8], [22.1, 2]]
      },{
        name: "Sample B",
        data: [
        [36.4, 13.4], [1.7, 11], [5.4, 8], [9, 17], [1.9, 4], [3.6, 12.2], [1.9, 14.4], [1.9, 9], [1.9, 13.2], [1.4, 7], [6.4, 8.8], [3.6, 4.3], [1.6, 10], [9.9, 2], [7.1, 15], [1.4, 0], [3.6, 13.7], [1.9, 15.2], [6.4, 16.5], [0.9, 10], [4.5, 17.1], [10.9, 10], [0.1, 14.7], [9, 10], [12.7, 11.8], [2.1, 10], [2.5, 10], [27.1, 10], [2.9, 11.5], [7.1, 10.8], [2.1, 12]]
      },{
        name: "Sample C",
        data: [
        [21.7, 3], [23.6, 3.5], [24.6, 3], [29.9, 3], [21.7, 20], [23, 2], [10.9, 3], [28, 4], [27.1, 0.3], [16.4, 4], [13.6, 0], [19, 5], [22.4, 3], [24.5, 3], [32.6, 3], [27.1, 4], [29.6, 6], [31.6, 8], [21.6, 5], [20.9, 4], [22.4, 0], [32.6, 10.3], [29.7, 20.8], [24.5, 0.8], [21.4, 0], [21.7, 6.9], [28.6, 7.7], [15.4, 0], [18.1, 0], [33.4, 0], [16.4, 0]]
      }],
      xaxis: {
        axisBorder: {
          color: colors.gridBorder,
        },
        axisTicks: {
          color: colors.gridBorder,
        },
        tickAmount: 10,
        labels: {
          formatter: function(val) {
            return parseFloat(val).toFixed(1)
          }
        }
      },
      yaxis: {
        tickAmount: 7
      }
    }

    var chart = new ApexCharts(
      document.querySelector("#apexScatter"),
      options
    );
    chart.render();
  }
  // Apex Scatter chart end




  // Apex Radialbar chart start
  if ($('#apexRadialBar').length) {
    var options = {
      chart: {
        height: 300,
        type: "radialBar",
        parentHeightOffset: 0,
        foreColor: colors.bodyColor,
        background: colors.cardBg,
        toolbar: {
          show: false
        },
      },
      theme: {
        mode: 'dark'
      },
      tooltip: {
        theme: 'dark'
      },
      colors: [colors.primary, colors.warning, colors.danger, colors.info],
      fill: {
        
      },
      grid: {
        padding: {
          top: 10
        }
      },
      plotOptions: {
        radialBar: {
          dataLabels: {
            total: {
              show: true,
              label: 'TOTAL',
              fontSize: '14px',
              fontFamily: fontFamily,
            }
          },
          track: {
            background: colors.gridBorder,
            strokeWidth: '100%',
            opacity: 1,
            margin: 5, 
        },
        }
      },
      series: [44, 55, 67, 83],
      labels: ["Apples", "Oranges", "Bananas", "Berries"],
      legend: {
        show: true,
        position: "top",
        horizontalAlign: 'center',
        fontFamily: fontFamily,
        itemMargin: {
          horizontal: 8,
          vertical: 0
        },
      },
    };
    
    var chart = new ApexCharts(document.querySelector("#apexRadialBar"), options);
    chart.render();
    var chartAreaBounds = chart.w.globals.dom.baseEl.querySelector('.apexcharts-inner').getBoundingClientRect();
  }
  // Apex Radialbar chart end  

});