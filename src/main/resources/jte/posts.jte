@import dev.aparikh.jsonplaceholder.dto.PostDto
@param java.util.List<PostDto> posts

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0" />
    <meta http-equiv="X-UA-Compatible" content="ie=edge" />
    <title>Posts</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            margin: 0;
            padding: 20px;
            color: #333;
        }
        .container {
            max-width: 800px;
            margin: 0 auto;
        }
        .post {
            margin-bottom: 30px;
            padding: 20px;
            border: 1px solid #ddd;
            border-radius: 5px;
            background-color: #f9f9f9;
        }
        .post-title {
            font-size: 24px;
            color: #2c3e50;
            margin-top: 0;
        }
        .post-meta {
            font-size: 14px;
            color: #7f8c8d;
            margin-bottom: 10px;
        }
        .post-body {
            line-height: 1.8;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Posts</h1>

        @if(posts.isEmpty())
            <p>No posts found.</p>
        @else
            @for(var post : posts)
                <div class="post">
                    <h2 class="post-title">${post.getTitle()}</h2>
                    <div class="post-meta">Post ID: ${post.getId()} | User ID: ${post.getUserId()}</div>
                    <div class="post-body">${post.getBody()}</div>
                </div>
            @endfor
        @endif
    </div>
</body>
</html>
