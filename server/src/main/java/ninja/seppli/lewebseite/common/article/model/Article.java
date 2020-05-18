package ninja.seppli.lewebseite.common.article.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import ninja.seppli.lewebseite.common.media.Media;
import ninja.seppli.lewebseite.common.permission.model.ApplicationUser;

/**
 * Represents a article
 *
 * @author Sebastian Zumbrunn
 * @version 1.0
 *
 */
@Entity
@Table
public class Article {
	/**
	 * the primary key
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * the title of the article
	 */
	@Column(nullable = false)
	private String title;

	/**
	 * the author who wrote it
	 */
	@ManyToOne(optional = false)
	private ApplicationUser author;

	/**
	 * The cached file, or null if no cached version exists
	 */
	@Column(nullable = true)
	private String cachedFile;

	/**
	 * The actual text in the formatting language (ie. markdown, latex, ...)
	 */
	@Column(nullable = false, columnDefinition = "text")
	private String text;

	@ManyToMany(mappedBy = "articles", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private List<Category> categories = new ArrayList<>();

	/**
	 * the header media of this article
	 */
	@ManyToMany(fetch = FetchType.LAZY , cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Media> headerImages = new HashSet<>();

	/**
	 * Constructor for hibernate
	 */
	protected Article() {
	}

	/**
	 * Constructor
	 *
	 * @param id         the id
	 * @param author     the author
	 * @param cachedFile the cached file (can be null)
	 * @param text       the text itself
	 */
	public Article(long id, String title, ApplicationUser author, String cachedFile, String text) {
		this.id = id;
		this.title = title;
		this.author = author;
		this.cachedFile = cachedFile;
		this.text = text;
	}

	/**
	 * Constructor
	 *
	 * @param author     the author
	 * @param cachedFile the cached file or null if none exists
	 * @param text       the text itself
	 */
	public Article(String title, ApplicationUser author, String cachedFile, String text) {
		this.id = null;
		this.title = title;
		this.author = author;
		this.cachedFile = cachedFile;
		this.text = text;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the author
	 */
	public ApplicationUser getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(ApplicationUser author) {
		this.author = author;
	}

	/**
	 * @return the cachedFile
	 */
	public String getCachedFile() {
		return cachedFile;
	}

	/**
	 * @param cachedFile the cachedFile to set
	 */
	public void setCachedFile(String cachedFile) {
		this.cachedFile = cachedFile;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Returns all catgegories in which this article is in
	 *
	 * @return the categories
	 */
	public List<Category> getCategories() {
		return categories;
	}

	/**
	 * @return the headerImages
	 */
	public Set<Media> getHeaderImages() {
		return headerImages;
	}

	/**
	 * @param headerImages the headerImages to set
	 */
	public void setHeaderImages(Set<Media> headerImages) {
		this.headerImages = headerImages;
	}
}
