$(function() {
  'use strict'

  // Mouse speed chart start
  var mrefreshinterval = 500; // update display every 500ms
  var lastmousex=-1; 
  var lastmousey=-1;
  var lastmousetime;
  var mousetravel = 0;
  var mpoints = [];
  var mpoints_max = 30;
  $('html').mousemove(function(e) {
      var mousex = e.pageX;
      var mousey = e.pageY;
      if (lastmousex > -1) {
          mousetravel += Math.max( Math.abs(mousex-lastmousex), Math.abs(mousey-lastmousey) );
      }
      lastmousex = mousex;
      lastmousey = mousey;
  });
  var mdraw = function() {
      var md = new Date();
      var timenow = md.getTime();
      if (lastmousetime && lastmousetime!=timenow) {
          var pps = Math.round(mousetravel / (timenow - lastmousetime) * 1000);
          mpoints.push(pps);
          if (mpoints.length > mpoints_max)
              mpoints.splice(0,1);
          mousetravel = 0;
          $('#mouseSpeedChart').sparkline(mpoints, { width: mpoints.length*2, tooltipSuffix: ' pixels per second', lineColor: 'rgb(101,113,255)' });
      }
      lastmousetime = timenow;
      setTimeout(mdraw, mrefreshinterval);
  }
  // We could use setInterval instead, but I prefer to do it this way
  setTimeout(mdraw, mrefreshinterval); 
  // Mouse speed chart start


$("#sparklineLine").sparkline([5,6,7,9,9,5,3,2,2,4,6,7,3,4,7,2,7,4,3,1,6,3,7 ], {type: 'line', width: '150', height: '50', fillColor: '', lineColor: 'rgb(255,51,102)'});
$("#sparklineArea").sparkline([5,6,7,9,9,5,3,2,2,4,6,7,3,4,7,2,7,4,3,1,6,3,7 ], {type: 'line', width: '150', height: '50', fillColor: 'rgba(102,209,209,.3)', lineColor: 'rgb(102,209,209)'});
$("#sparklineBar").sparkline([5,6,7,9,9,5,3,2,2,4,6,7,3,4,7,2,7,4,3,1,6,3,7 ], {type: 'bar', width: '150', height: '50', barColor: 'rgb(255,51,102)'});
$("#sparklineBarStacked").sparkline([[5,2],[6,4],[9,2],[9,5],[5,2],[3,7],[2,7],[2,2],[4,3],[6,5],[7,3],[3,2],[4,9],[7,1],[2,2],[7,2],[4,4],[3,3],[1,9],[6,8],[3,2],[7,1] ], {type: 'bar', width: '150', height: '50',stackedBarColor: ['rgb(255,51,102)', "rgb(102,209,209)"]});
$("#sparklineComposite").sparkline([5,6,9,9,5,3,2,2,4,6,7,3,4,7,2,7,4,3,1,6,3,7], {type: 'bar', width: '150', height: '50', barColor: 'rgb(255,51,102)'});
$("#sparklineComposite").sparkline([2,4,2,5,2,7,7,2,3,5,3,2,9,1,2,2,4,3,9,8,2,1], {type: 'line', width: '150', height: '50', composite: true, fillColor: 'rgba(102,209,209,.3)', lineColor: 'rgb(102,209,209)'});
$("#sparklineBoxplot").sparkline([5,6,7,9,9,5,3,2,2,4,6,7,3,4,7,2,7,4,3,1,6,3,7 ], {type: 'box', width: '150', height: '40', boxFillColor: "rgba(247, 126, 185, .3)", boxLineColor: "rgb(255,51,102)", whiskerColor: "rgb(255,51,102)"});
$("#sparklinePie").sparkline([1,3,2], {type: 'pie', width: '150', height: '50', sliceColors: ['rgb(255,51,102)', 'rgb(102,209,209)', 'rgb(101,113,255)']});
$("#sparklineBullet").sparkline([5,6,7,9,1], {type: 'bullet', width: '150', height: '50', performanceColor: 'rgb(255,51,102)', rangeColors: ['rgba(247, 126, 185, .1)', 'rgba(247, 126, 185, .2)', 'rgba(247, 126, 185, .3)']});


});