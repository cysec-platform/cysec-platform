<%--
  #%L
  CYSEC Platform Core
  %%
  Copyright (C) 2020 - 2024 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  --%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8"/>
    <title>404 -- Document not found</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">
    <meta name="keywords" content="">
    <meta name="generator" content="Handcraftet by some fool">

    <!-- styles -->
    <link href="${pageContext.request.contextPath}/css/bootstrap-3.3.7-dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/prettify.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/navigation.css" rel="stylesheet">

    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="${pageContext.request.contextPath}//js/html5shiv.min.js"></script>
    <![endif]-->

    <!-- Fav and touch icons -->
    <link rel="apple-touch-icon" sizes="57x57" href="${pageContext.request.contextPath}/assets/favicon/apple-icon-57x57.png">
    <link rel="apple-touch-icon" sizes="60x60" href="${pageContext.request.contextPath}/assets/favicon/apple-icon-60x60.png">
    <link rel="apple-touch-icon" sizes="72x72" href="${pageContext.request.contextPath}/assets/favicon/apple-icon-72x72.png">
    <link rel="apple-touch-icon" sizes="76x76" href="${pageContext.request.contextPath}/assets/favicon/apple-icon-76x76.png">
    <link rel="apple-touch-icon" sizes="114x114" href="${pageContext.request.contextPath}/assets/favicon/apple-icon-114x114.png">
    <link rel="apple-touch-icon" sizes="120x120" href="${pageContext.request.contextPath}/assets/favicon/apple-icon-120x120.png">
    <link rel="apple-touch-icon" sizes="144x144" href="${pageContext.request.contextPath}/assets/favicon/apple-icon-144x144.png">
    <link rel="apple-touch-icon" sizes="152x152" href="${pageContext.request.contextPath}/assets/favicon/apple-icon-152x152.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/favicon/apple-icon-180x180.png">
    <link rel="icon" type="image/png" sizes="192x192"  href="${pageContext.request.contextPath}/assets/favicon/android-icon-192x192.png">
    <link rel="icon" type="image/png" sizes="32x32" href="${pageContext.request.contextPath}/assets/favicon/favicon-32x32.png">
    <link rel="icon" type="image/png" sizes="96x96" href="${pageContext.request.contextPath}/assets/favicon/favicon-96x96.png">
    <link rel="icon" type="image/png" sizes="16x16" href="${pageContext.request.contextPath}/assets/favicon/favicon-16x16.png">
    <meta name="msapplication-TileColor" content="#ffffff">
    <meta name="msapplication-TileImage" content="${pageContext.request.contextPath}/assets/favicon/ms-icon-144x144.png">
    <meta name="theme-color" content="#ffffff">

    <link rel="shortcut icon" href="${pageContext.request.contextPath}/assets/favicon/favicon.ico">

    <!-- Extra CSS for additional functionality -->
    <link href="${pageContext.request.contextPath}/css/tingle.css" rel="stylesheet">
    
    <style>
     * {
         -moz-box-sizing:border-box;
         -webkit-box-sizing:border-box;
         box-sizing:border-box;
     }

     html, body, div, span, object, iframe, h1, h2, h3, h4, h5, h6, p, blockquote, pre,
     abbr, address, cite, code, del, dfn, em, img, ins, kbd, q, samp,
     small, strong, sub, sup, var, b, i, dl, dt, dd, ol, ul, li,
     fieldset, form, label, legend, caption, article, aside, canvas, details, figcaption, figure,  footer, header, hgroup,
     menu, nav, section, summary, time, mark, audio, video {
         margin:0;
         padding:0;
         border:0;
         outline:0;
         vertical-align:baseline;
         background:transparent;
     }

     article, aside, details, figcaption, figure, footer, header, hgroup, nav, section {
         display: block;
     }

     html {
         font-size: 16px;
         line-height: 24px;
         width:100%;
         height:100%;
         -webkit-text-size-adjust: 100%;
         -ms-text-size-adjust: 100%;
         overflow-y:scroll;
         overflow-x:hidden;
     }

     img {
         vertical-align:middle;
         max-width: 100%;
         height: auto;
         border: 0;
         -ms-interpolation-mode: bicubic;
     }

     body {
         min-height:100%;
         -webkit-font-smoothing: subpixel-antialiased;
     }

     .clearfix {
	       clear:both;
	       zoom: 1;
     }
     .clearfix:before, .clearfix:after {
         content: "0";
         display: block;
         height: 0;
         visibility: hidden;
     }
     .clearfix:after {
         clear: both;
     }

 .sign.error-page-wrapper {
    font-family: 'Renner', sans-serif;
    position:relative;
  }

 .sign.error-page-wrapper .sign-container {
    width:450px;
    height:415px;
    margin:0 auto;
    position: relative;
    transform:rotate(-20deg);
    text-indent:-20px;
  }


 .sign.error-page-wrapper .sign-container .nob {
    height:44px;
    width:44px;
    border-radius: 99px;
    border:12px solid #343c3f;
    position: absolute;
    top:0px;
    left:50%;
    margin-left:-22px;
  }

 .sign.error-page-wrapper .sign-container .post {
    transition:background-color .5s linear;
    width: 190px;
    height: 15px;
    top: 71px;
    background-color: #343c3f;
  }

 .sign.error-page-wrapper .sign-container .post.left {
    position: absolute;
    transform:rotate(-30deg);
    left:35px;
  }

 .sign.error-page-wrapper .sign-container .post.right {
    position: absolute;
    transform:rotate(30deg);
    right:35px;
  }

 .sign.error-page-wrapper .sign-container .pane {
    transition:background-color .5s linear, border-color .5s linear;
    box-shadow: 0 5px 0 rgba(0,0,0,.1) inset, 5px 0 0 rgba(0,0,0,.1) inset, 15px 15px 0 rgba(0,0,0,.1);
    border:20px solid #343c3f;
    height:300px;
    text-align: center;
    position: absolute;
    top: 115px;
    left:0px;
    right:0px;
  }

 .sign.error-page-wrapper .sign-container .pane .headline {
    transition:color .5s linear;
    margin-top:65px;
    margin-bottom: 10px;
    font-size:54px;
    line-height:68px;
    font-weight:600;
    letter-spacing: -2px;
    text-transform: uppercase;
  }

 .sign.error-page-wrapper .sign-container .pane.just-header .headline {
    margin-top:100px;
  }

 .sign.error-page-wrapper .sign-container .pane .context {
    transition:color .5s linear;
    font-size:24px;
    line-height: 32px;
  }


  @media screen and (max-width: 500px) {
    .sign.error-page-wrapper {
      padding-top:10%;
    }
    .sign.error-page-wrapper .sign-container {
      width:280px;
      top: 0px;
    }
    .sign.error-page-wrapper .sign-container .post {
      width:100px;
      top:50px;
    }
    .sign.error-page-wrapper .sign-container .pane {
      top:70px;
      height:220px;
    }
    .sign.error-page-wrapper .sign-container .pane .headline {
      margin-top:20px;
      font-size:45px;
      margin-bottom: 6px;
    }
    .sign.error-page-wrapper .sign-container .pane.just-header .headline {
      margin-top: 39px;
      line-height: 55px;
    }
    .sign.error-page-wrapper .sign-container .pane .context {
      font-size:20px;
      line-height: 28px;
    }
  }

 .sign.error-page-wrapper .text-container {
    max-width:425px;
    position: absolute;
    bottom:100px;
    left:35px;
  }

 .sign.error-page-wrapper .text-container .headline {
    transition:color .5s linear;
    font-size:40px;
    line-height: 52px;
    letter-spacing: -1px;
    margin-bottom: 5px;
  }

 .sign.error-page-wrapper .text-container .context {
    transition:color .5s linear;
    font-size:18px;
    line-height:27px;
  }
 .sign.error-page-wrapper .text-container .context p {
    margin:0;
  }
 .sign.error-page-wrapper .text-container .context p + p {
    margin-top:10px;
  }
 .sign.error-page-wrapper .buttons-container {
    margin-top: 20px;
  }

 .sign.error-page-wrapper .buttons-container a {
    transition: text-indent .2s linear, color .5s linear, border-color .5s linear;
    font-size:16px;
    text-transform: uppercase;
    text-decoration: none;
    border:2px solid black;
    border-radius: 99px;
    padding:9px 0 10px;
    width:195px;
    overflow: hidden;
    text-align:center;
    display:inline-block;
    position: relative;
  }

 .sign.error-page-wrapper .buttons-container a:hover {
    background-color:rgba(255,255,255,.1);
    text-indent: 17px;
  }

 .sign.error-page-wrapper .buttons-container a:first-child {
    margin-right:25px;
  }

 .sign.error-page-wrapper .buttons-container .fa {
    transition:left .2s ease-out;
    position: absolute;
    left:-50px;
  }

 .sign.error-page-wrapper .buttons-container .fa-warning {
    font-size:16px;
    top:14px;
  }

 .sign.error-page-wrapper .buttons-container a:hover .fa-warning {
    left:0px;
  }

 .sign.error-page-wrapper .buttons-container .fa-power-off {
    font-size:16px;
    top:14px;
  }

 .sign.error-page-wrapper .buttons-container a:hover .fa-power-off {
    left:0px;
  }

 .sign.error-page-wrapper .buttons-container .fa-home {
    font-size:18px;
    top:12px;
  }

 .sign.error-page-wrapper .buttons-container a:hover .fa-home {
    left:25px;
  }

  @media screen and (max-width: 500px) {
   .sign.error-page-wrapper .text-container {
      bottom:20px;
      left:20px;
      right:20px;
    }
   .sign.error-page-wrapper .text-container .header {
      font-size:32px;
      line-height:40px;
    }
   .sign.error-page-wrapper .text-container .context {
      font-size:15px;
      line-height: 22px;
    }
   .sign.error-page-wrapper .buttons-container {
      overflow: hidden;
    }
   .sign.error-page-wrapper .buttons-container a {
      font-size:14px;
      padding:8px 0 9px;
      width:45%;
      float:left;
      margin:0;
    }
   .sign.error-page-wrapper .buttons-container a + a {
      float:right;
    }
   .sign.error-page-wrapper .buttons-container a:hover {
      text-indent: 0px;
    }
   .sign.error-page-wrapper .buttons-container .fa {
      display:none;
    }
  }

    .background-color {
      background-color: rgba(255, 255, 255, 1); 
    }


    .primary-text-color {
      color: rgba(0, 0, 0, 1) ;
    }

    .secondary-text-color {
      // color: #73c5df ;
    }

    .sign-text-color {
      color: rgba(210, 235, 245, 1) ;
    }

    .sign-frame-color {
      color: #343C3F;
    }

    .pane {
      background-color: #73c5df ;
    }

    .border-button {
      color: rgba(0, 0, 0, 1) ;
      border-color: rgba(0, 0, 0, 1) ;
    }
    .button {
      background-color: rgba(0, 0, 0, 1) ;
    }


    </style>
  </head>
  <body class="sign error-page-wrapper background-color background-image"  onload="prettyPrint()">
   		<!-- Fixed navbar -->
    <div class="navbar navbar-default navbar-fixed-top" role="navigation">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-left" href="/">
            <img src="${pageContext.request.contextPath}/assets/logo/CYSEC_Logo_RGB.svg" alt="The SMESEC project"/>
          </a>
        </div>
        <div class="clear"></div>
        <div class="navbar-collapse collapse">
        </div><!--/.nav-collapse -->
      </div>
    </div>

    <div class="wrap">
      <div class="container">
        <div class="sign-container">
	  	  <div class="nob"></div>
		  <div class="post left"></div>
		  <div class="post right"></div>
		  <div class="pane">
			<div class="headline sign-text-color">
				404
			</div>
			<div class="context sign-text-color">
				Sorry ... but this document is missing

			</div>
		  </div>
	    </div>
	    <div class="text-container">
		  <div class="headline secondary-text-color">
			404
		  </div>
		  <div class="context primary-text-color">
			<p>
				We were unable to find this document. If you think that this was an error then we are glad for a hint.
			</p>
		  </div>
		  <div class="buttons-container">
			<a class="border-button" href="/"><span class="fa fa-home"></span> Home page</a>
			<a class="border-button" href="mailto:support@smesec.eu?subject=Broken%20link" target="_blank"><span class="fa fa-warning"></span> Report problem</a>
		  </div>
	    </div>
	  </div>
	</div>  
    <script src="${pageContext.request.contextPath}/js/jquery-1.11.1.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/jquery-ui.min.js"></script>
	<script src="${pageContext.request.contextPath}/js/bootstrap.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/prettify.js"></script>
    <script src="${pageContext.request.contextPath}/js/errorpage.js"></script>
  </body>
</html>
