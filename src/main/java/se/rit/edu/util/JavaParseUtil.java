package se.rit.edu.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.comments.CommentsCollection;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JavaParseUtil {

    public static List<GroupedComment> parseFileForComments(InputStream file) {
        final JavaParser parser = new JavaParser();
        final ParseResult parsedFile = parser.parse(file);
        final List<GroupedComment> allComments = parsedFile.getCommentsCollection().isPresent() ?
                ((CommentsCollection)parsedFile.getCommentsCollection().get())
                        .getComments()
                        .stream()
                        .filter(comment -> !comment.isJavadocComment())
                        .map(GroupedComment::new)
                        .sorted()
                        .collect(Collectors.toList())
                : new ArrayList<>();

        final List<GroupedComment> groupedComments = new ArrayList<>();
        GroupedComment previousComment = null;
        for( GroupedComment thisComment : allComments ) {
            if( previousComment != null && previousComment.precedesDirectly(thisComment) ) {
                previousComment = previousComment.joinWith(thisComment);
            } else {
                // Previous comment was the last of the group, so add it to the list
                if( previousComment != null ) {
                    groupedComments.add(previousComment);
                }
                // restart grouping with the current comment
                previousComment = thisComment;
            }
        }
        if( previousComment != null && !groupedComments.contains(previousComment) ) {
            groupedComments.add(previousComment);
        }
        return new ArrayList<>(groupedComments);
    }
}
