<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Audit widget</title>
        <style>
            line {
                /*overwrite style from SVG*/
                stroke : #007EA7 !important;
            }
        </style>
    </head>
    <body>
        <h1>Current score and grade</h1>
        <div>
            <p>Score:${it.score}</p>
            <p>Grade:${it.grade}</p>
        </div>
        <h1>Recent activity</h1>
        ${it.audits}
    </body>
</html>