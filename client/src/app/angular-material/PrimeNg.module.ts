import { NgModule } from '@angular/core';
import { ChartModule } from 'primeng/chart';
import { TableModule } from 'primeng/table';
import { PaginatorModule } from 'primeng/paginator';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { CardModule } from 'primeng/card';

@NgModule({
  exports: [
    ChartModule,
    TableModule,
    PaginatorModule,
    ButtonModule,
    InputTextModule,
    CardModule
  ]
})
export class PrimeNgModule { }
