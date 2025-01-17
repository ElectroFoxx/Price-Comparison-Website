import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import { provideHttpClient } from '@angular/common/http';
import { importProvidersFrom, NgModule } from '@angular/core';

@NgModule({ providers: [ ...appConfig.providers ] })
export class AppModule {}

bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(),
    importProvidersFrom(AppModule)
  ]
})
  .catch((err) => console.error(err));
