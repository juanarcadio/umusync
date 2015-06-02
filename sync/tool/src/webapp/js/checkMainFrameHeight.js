setTimeout(function() {
 var currentHeight = top.document.getElementById(top.frames[0].name).style.height;
 currentHeight = currentHeight.substring(0,currentHeight.length-2);
 if (Number(currentHeight) < 100) {
   var body = document.body, html = document.documentElement;
   var height = Math.max( body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight, html.offsetHeight ); 
   top.document.getElementById(top.frames[0].name).style.height=height+'px';
 }
},1000);