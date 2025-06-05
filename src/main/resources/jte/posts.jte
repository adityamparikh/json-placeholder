@import dev.aparikh.jsonplaceholder.dto.PostDto
@param java.util.List<PostDto> posts

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0"/>
    <meta http-equiv="X-UA-Compatible" content="ie=edge"/>
    <title>Posts</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <!-- Custom styles to enhance Bootstrap -->

    <style>
        body {
            padding-top: 2rem;
            padding-bottom: 2rem;
            background-color: #f8f9fa;
        }

        .post-meta {
            color: #6c757d;
        }
    </style>

</head>

<body>
<div class="container py-4">
    <header class="pb-3 mb-4 border-bottom">
        <h1 class="display-5 fw-bold">Posts</h1>
    </header>

    @if(posts.isEmpty())
        <div class="alert alert-info" role="alert">
            No posts found.
        </div>
    @else
        <div class="row row-cols-1 g-4">
            @for(var post : posts)
                <div class="col">
                    <div class="card shadow-sm">
                        <div class="card-body">
                            <h2 class="card-title h4">${post.getTitle()}</h2>
                            <div class="post-meta small mb-2">
                                <span class="badge bg-secondary">Post ID: ${post.getId()}</span>
                                <span class="badge bg-primary">User ID: ${post.getUserId()}</span>
                            </div>
                            <p class="card-text">${post.getBody()}</p>
                        </div>
                    </div>
                </div>
            @endfor
        </div>
    @endif
</div>

<!-- Bootstrap JS Bundle with Popper -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
