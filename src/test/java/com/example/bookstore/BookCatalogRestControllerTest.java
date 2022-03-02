package com.example.bookstore;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.entity.Book;
import com.example.bookstore.service.BookCatalogService;
import com.fasterxml.jackson.databind.ObjectMapper;


@SpringBootTest(
		 classes = BookstoreSpringDataApplication.class,
	        webEnvironment = WebEnvironment.MOCK
	)
@AutoConfigureMockMvc
public class BookCatalogRestControllerTest {
	@Autowired
    MockMvc mockMvc;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    BookCatalogService bookCatalogService;
    
    @Test
    void findByIsbn() throws Exception {
        var bookCatalog = new BookResponse(
                1L, "456123789", "Author",
                "Title", 350, 2010, 45.50, "Cover"

        );
        Mockito.when(bookCatalogService.findBookByIsbn("456123789"))
                .thenReturn(bookCatalog);
        mockMvc.perform(get("/books/456123789")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.isbn", containsString("456123789")))
                .andExpect(jsonPath("$.author", is("Author")))
                .andExpect(jsonPath("$.title", is("Title")))
                .andExpect(jsonPath("$.pages", is(350)))
                .andExpect(jsonPath("$.year", is(2010)))
                .andExpect(jsonPath("$.price", is(45.50)))
                .andExpect(jsonPath("$.cover", is("Cover")));
    }

    @Test
    void deleteByIsbn() throws Exception {
        var bookResponse = new BookResponse(
                1L, "456123789", "Author",
                "Title", 350, 2010, 45.50, "Cover"
        );
        Mockito.when(bookCatalogService.deleteBook("456123789"))
                .thenReturn(bookResponse);
        mockMvc.perform(delete("/books/456123789")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.isbn", containsString("456123789")))
                .andExpect(jsonPath("$.author", is("Author")))
                .andExpect(jsonPath("$.title", is("Title")))
                .andExpect(jsonPath("$.pages", is(350)))
                .andExpect(jsonPath("$.year", is(2010)))
                .andExpect(jsonPath("$.price", is(45.50)))
                .andExpect(jsonPath("$.cover", is("Cover")));
    }

    @Test
    void findAllBooks() throws Exception {
        var bookResponse = new BookResponse(
                1L, "456123789", "Author",
                "Title", 350, 2010, 45.50, "Cover"
        );
        var bookResponse2 = new BookResponse(
                2L, "123456789", "Author2",
                "Title2", 200, 2000, 65.95, "Cover2"
        );

        Mockito.when(bookCatalogService.findAll(0, 2))
                .thenReturn(List.of(bookResponse, bookResponse2));
        mockMvc.perform(get("/books?pageNo=0&pageSize=2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[0].isbn", is("456123789")))
                .andExpect(jsonPath("$[1].isbn", is("123456789")));
    }

    @Test
    void addBook() throws Exception {
        var book = new Book();
        book.setIsbn("456123789");
        book.setPages(500);
        book.setPrice(55.55);
        book.setAuthor("Author3");
        book.setTitle("Title3");
        book.setYear(2005);

        Mockito.when(bookCatalogService.addBook(modelMapper.map(book, BookRequest.class)))
                .thenReturn(modelMapper.map(book, BookResponse.class));
        mockMvc.perform(post("/books")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk());
    }

    @Test
    void updateBook() throws Exception {
        var book = new Book();
        book.setIsbn("456123789");
        book.setPages(350);
        book.setPrice(45.50);
        book.setAuthor("Author");
        book.setTitle("Title");
        book.setYear(2000);

        Mockito.when(bookCatalogService.updateBook(modelMapper.map(book, BookRequest.class)))
                .thenReturn(modelMapper.map(book, BookResponse.class));

        mockMvc.perform(put("/books/" + book.getIsbn())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk());
    }
	
}
