package org.springframework.samples.petclinic.web;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SpringJUnitWebConfig(locations = {"classpath:spring/mvc-test-config.xml", "classpath:spring/mvc-core-config.xml"})
class OwnerControllerTest {

    @Autowired
    OwnerController ownerController;

    @Autowired
    ClinicService clinicService;

    MockMvc mockMvc;

    @Captor
    ArgumentCaptor<String> stringArgumentCaptor;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ownerController).build();
    }

    //We go this error so we needed reset our clinic service
    //org.mockito.exceptions.verification.TooManyActualInvocations:
    //clinicService.findOwnerByLastName(
    //    <Capturing argument>
    //);
    @AfterEach
    void tearDown()
    {
        reset(clinicService);
    }



    //Here we edit owner 1 with new details with valid info
    @Test
    void testProcessUpdateOwnerFormPostValid() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/edit", 1)
                .param("firstName", "Jacques")
                .param("lastName", "vdMerwe")
                .param("address", "Somewhere")
                .param("city", "somecity")
                .param("telephone", "034043044"))
                .andExpect(status().is3xxRedirection())
        ;
    }

    //Here we edit owner 1 with new details with valid info
    @Test
    void testProcessUpdateOwnerFormPostInValid() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/edit", 1)
                .param("address", "Somewhere")
                .param("city", "somecity")
                .param("telephone", "034043044"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("owner"))
                .andExpect(model().attributeHasFieldErrors("owner","firstName","lastName"))
                .andExpect(view().name("owners/createOrUpdateOwnerForm"))
        ;
    }

    //Here we use the post method with processCreationForm with new owner
    @Test
    void testNewOwnerPostValid() throws Exception {

        mockMvc.perform(post("/owners/new")
                .param("firstName", "Jacques")
                .param("lastName", "vdMerwe")
                .param("address", "Somewhere")
                .param("city", "somecity")
                .param("telephone", "034043044")
        )
                .andExpect(status().is3xxRedirection())
           ;
    }

    //Validate invalid Owner object with specific fields that are missing and we check so in the test
    @Test
    void testNewOwnerPostInValid() throws Exception {
        mockMvc.perform(post("/owners/new")
                    .param("firstName", "Jacques")
                    .param("lastName", "vdMerwe")
                    .param("address", "Somewhere"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("owner"))
                .andExpect(model().attributeHasFieldErrors("owner","city","telephone"))
                .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    void testFindByNameNotFound() throws Exception {
        mockMvc.perform(get("/owners")
                    .param("lastName", "Dont find ME!"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/findOwners"));


    }

    @Test
    void initCreationFormTest() throws Exception {
        mockMvc.perform(get("/owners/new"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("owner"))
            .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    void returnListOfOwners() throws Exception {
        given(clinicService.findOwnerByLastName("")).willReturn(Lists.newArrayList(new Owner(), new Owner()));
        mockMvc.perform(get("/owners"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/ownersList"));

        //Capture the value that is passed to method findOwnerByLastName
        //To use this capture we have to extend the test framework with
        //@ExtendWith(MockitoExtension.class) els the result will be null error
        then(clinicService).should().findOwnerByLastName(stringArgumentCaptor.capture());

        //This will test that we do call the method with no arguments
        assertThat(stringArgumentCaptor.getValue()).isEqualToIgnoringCase("");
    }

    @Test
    void returnOneOwner() throws Exception {
        Owner owner = new Owner();
        owner.setId(1);
        owner.setLastName("find just one");

        given(clinicService.findOwnerByLastName("find just one")).willReturn(Lists.newArrayList(owner));
        mockMvc.perform(get("/owners")
                        .param("lastName", "find just one"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/owners/1"));
    }
}