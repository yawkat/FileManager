package at.yawk.filemanager;

/**
 * @author Jonas Konrad (yawkat)
 */
public interface Constants {
    static final String EXTRA_FILE = "file";
    static final String EXTRA_NO_EXIT_ANIMATION = "no_animation";
    static final String EXTRA_SELECTION = "select";
    static final String EXTRA_SELECTION_BUTTON_TITLE = "select.submit";
    static final int EXTRA_SELECTION_NONE = 0;
    static final int EXTRA_SELECTION_DIRECTORY = 1;
    static final int EXTRA_SELECTION_FILE = 2;

    static final String PREFERENCE_SORTER = "sorter";
    static final String PREFERENCE_SORTER_REVERSE = "sorter.reverse";
    static final String PREFERENCE_SORTER_DIRECTORIES_FIRST = "sorter.directories_first";
    static final String PREFERENCE_SHOW_HIDDEN = "hidden";
    static final String PREFERENCE_SHOW_FOLDER_SIZE = "folder_size";

    static final int SORTER_SIZE = 0;
    static final int SORTER_NAME = 1;
    static final int SORTER_NAME_INTELLIGENT = 2;
    static final int SORTER_MODIFY_DATE = 3;
    static final int SORTER_RANDOM = 4;
}
