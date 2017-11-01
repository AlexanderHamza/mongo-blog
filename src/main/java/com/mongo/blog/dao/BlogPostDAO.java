package com.mongo.blog.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BlogPostDAO {
    private final MongoCollection<Document> postsCollection;

    public BlogPostDAO(final MongoDatabase blogDatabase) {
        postsCollection = blogDatabase.getCollection("posts");
    }

    // Return a single post corresponding to a permalink
    public Document findByPermalink(String permalink) {
        return postsCollection.find(Filters.eq("permalink", permalink)).first();
    }

    // Return a list of posts in descending order. Limit determines
    // how many posts are returned.
    public List<Document> findByDateDescending(int limit) {
        return postsCollection.find().sort(new Document("date", -1)).limit(limit).into(new ArrayList<>());
    }

    public String addPost(String title, String body, List tags, String username) {

        System.out.println("inserting blog entry " + title + " " + body);

        String permalink = title.replaceAll("\\s", "_"); // whitespace becomes _
        permalink = permalink.replaceAll("\\W", ""); // get rid of non alphanumeric
        permalink = permalink.toLowerCase();

        // Build the post object and insert it
        Document post = new Document()
                .append("author", username)
                .append("body", body)
                .append("comments", new ArrayList<>())
                .append("date", new Date())
                .append("permalink", permalink)
                .append("tags", tags)
                .append("title", title);


        postsCollection.insertOne(post);

        return permalink;
    }

    // Append a comment to a blog post
    public void addPostComment(final String name, final String email, final String body, final String permalink) {
        final Document comment = new Document().append("author", name).append("body", body);

        if (email != null) {
            comment.append("email", email);
        }

        final Document filter = new Document("permalink", permalink);
        final Document command = new Document("$push", new Document("comments", comment));

        postsCollection.findOneAndUpdate(filter, command);

    }
}
