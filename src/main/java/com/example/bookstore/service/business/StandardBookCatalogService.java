package com.example.bookstore.service.business;

import java.util.Collection;

import org.apache.tomcat.util.codec.binary.Base64;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.entity.Book;
import com.example.bookstore.exception.RestExceptionBase;
import com.example.bookstore.repository.BookCatalogRepository;
import com.example.bookstore.service.BookCatalogService;
import java.util.Comparator;

@Service
public class StandardBookCatalogService implements BookCatalogService {

	private BookCatalogRepository bookCatalogRepository;
	private ModelMapper modelMapper;

	public StandardBookCatalogService(BookCatalogRepository bookCatalogRepository, ModelMapper modelMapper) {
		this.bookCatalogRepository = bookCatalogRepository;
		this.modelMapper = modelMapper;
	}

	@Override
	public BookResponse findBookByIsbn(String isbn) {
        return modelMapper.map(bookCatalogRepository.
                        findByIsbn(isbn).orElseThrow(() ->
                                new RestExceptionBase("Cannot find the book!", "unknown.book", "1")),
                BookResponse.class
        );
    }

	@Override
    @Transactional
    public BookResponse deleteBook(String isbn) {
        var managedBook = bookCatalogRepository.findByIsbn(isbn).orElseThrow(() ->
                new RestExceptionBase("Cannot delete the book!", "unknown.book", "2"));
        bookCatalogRepository.delete(managedBook);
        return modelMapper.map(managedBook, BookResponse.class);

    }

	@Override
    public Collection<BookResponse> findAll(int pageNo, int pageSize) {
        return bookCatalogRepository.findAll(PageRequest.of(pageNo, pageSize))
                .stream()
                .map(book -> modelMapper.map(book, BookResponse.class))
                .sorted(Comparator.comparing(BookResponse::author))
                .toList();
    }

	@Override
    @Transactional
    public BookResponse addBook(BookRequest book) {
        try {
            var managedBook = modelMapper.map(book, Book.class);
            return modelMapper.map(bookCatalogRepository.save(managedBook), BookResponse.class);
        } catch (Exception e) {
            throw new RestExceptionBase("Cannot insert book!", "duplicate.isbn", "3");
        }
    }

	@Override
    @Transactional
    public BookResponse updateBook(BookRequest book) {
        String isbn = book.getIsbn();
        var managedBook = bookCatalogRepository.findByIsbn(isbn)
                .orElseThrow(() -> new RestExceptionBase("Cannot find the book!", "unknown.book", "4"));
        managedBook.setPrice(managedBook.getPrice());
        managedBook.setPages(managedBook.getPages());
        managedBook.setCover(Base64.decodeBase64(book.getCover()));
        bookCatalogRepository.saveAndFlush(managedBook);
        return modelMapper.map(managedBook, BookResponse.class);
    }

}
