package northwind.ui.example;

import reactive.ui.*;
import android.os.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import tee.binding.*;
import tee.binding.it.*;
import tee.binding.task.*;

public class ActivityPromptProduct extends Activity {
	Layoutless layoutless;
	Note seekProduct = new Note().afterChange(new CannyTask() {
		@Override
		public void doTask() {
			if (grid != null) {
				grid.clearColumns();
				requery();
				grid.refresh();
			}
		}
	}.laziness.is(200), true);
	ColumnDescription columnName = new ColumnDescription();
	ColumnDescription columnSupplier = new ColumnDescription();
	ColumnText columnPrice = new ColumnText();
	ColumnDescription columnStock = new ColumnDescription();
	DataGrid grid;
	Numeric offset = new Numeric().value(0);
	int pageSize = 25;
	Bough data;
	Expect refresh = new Expect().status.is("Wait...").task.is(new Task() {
		@Override
		public void doTask() {
			grid.clearColumns();
			requery();
		}
	})//
	.afterDone.is(new Task() {
		@Override
		public void doTask() {
			grid.refresh();
		}
	});

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		layoutless = new Layoutless(this);
		setContentView(layoutless);
		this.setTitle("Northwind UI Example - Products");
		compose();
		refresh.start(this);
	}
	void compose() {
		layoutless.child(new Decor(this)//
				.background.is(Auxiliary.colorBackground)//
						.sketch(new SketchPlate().tint(new TintBitmapTile()//
								.bitmap.is(BitmapFactory.decodeResource(getResources(), R.drawable.snowflakes3))//
								)//
						.width.is(layoutless.width().property)//
						.height.is(layoutless.height().property)//
						)//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property)//
				);
		layoutless.child(new Decor(this)//
				.labelText.is("Products")//
				.labelSize.is(0.2 * Auxiliary.screenWidth(this))//
				.labelColor.is(0x22006699)//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property)//
				);
		int xx = (Auxiliary.screenWidth(this) - Auxiliary.tapSize * 5) / 2;
		if (xx < 0) {
			xx = 0;
		}
		layoutless.child(new Decor(this)//
				.bitmap.is(BitmapFactory.decodeResource(getResources(), R.drawable.zoom1))//
						.left().is(xx - 150)//
						.width().is(300)//
						.height().is(300)//
				);
		layoutless.child(new RedactText(this)//
				.text.is(seekProduct)//
						.left().is(xx)//
						.top().is(0.1 * Auxiliary.tapSize)//
						.width().is(Auxiliary.tapSize * 5)//
						.height().is(Auxiliary.tapSize * 0.8)//
				);
		grid = new DataGrid(this);
		layoutless.child(grid//
				.headerHeight.is(0.5 * Auxiliary.tapSize)//
				.pageSize.is(pageSize)//
				.dataOffset.is(offset)//
				.beforeFlip.is(new Task() {
					@Override
					public void doTask() {
						System.out.println("grid beforeFlip");
						requery();
					}
				})//
				.center.is(true)//
						.columns(new Column[] { columnName.title.is("Product").width.is(5 * Auxiliary.tapSize)//
								, columnSupplier.title.is("Supplier").width.is(4 * Auxiliary.tapSize)//
								, columnPrice.title.is("Price").width.is(1 * Auxiliary.tapSize)//
								, columnStock.title.is("Stock").width.is(2 * Auxiliary.tapSize) //
								})//
						.left().is(1)//
						.top().is(Auxiliary.tapSize)//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property.minus(Auxiliary.tapSize))//
				);
	}
	void tapProduct(String productID) {
		Intent intent = this.getIntent();
		intent.putExtra("productID", productID);
		this.setResult(RESULT_OK, intent);
		finish();
	}
	void requery() {
		String sql = "select "//
				+ "\n	Products.ProductID,Products.ProductName,Products.QuantityPerUnit,Products.UnitPrice,Products.UnitsInStock"//
				+ "\n	,Suppliers.CompanyName,Suppliers.Country,Suppliers.City"//
				+ "\n	,Categories.CategoryName,Categories.Description"//
				+ "\n	from Products"//
				+ "\n	join Categories on Categories.CategoryID=Products.CategoryID"//
				+ "\n	join Suppliers on Suppliers.SupplierID=Products.SupplierID";
		String seek = seekProduct.value().trim().toUpperCase();
		sql = sql + "\n	where upper(Products.ProductName) like '%" + seek + "%' or upper(Categories.Description) like '%" + seek + "%'";
		sql = sql + "\n	order by Categories.Description,Products.ProductName"//
				+ "\n limit " + (pageSize * 3) + " offset " + offset.value().intValue();
		data = Auxiliary.fromCursor(Tools.db(this).rawQuery(sql, null), true);
		for (int i = 0; i < data.children.size(); i++) {
			Bough row = data.children.get(i);
			final String productID = row.child("ProductID").value.property.value();
			Task tapOrder = new Task() {
				@Override
				public void doTask() {
					tapProduct(productID);
				}
			};
			columnName.cell(row.child("ProductName").value.property.value(), tapOrder, row.child("Description").value.property.value());
			columnSupplier.cell(row.child("CompanyName").value.property.value(), tapOrder, row.child("City").value.property.value() + ", " + row.child("Country").value.property.value());
			columnPrice.cell(row.child("UnitPrice").value.property.value(), tapOrder);
			columnStock.cell(row.child("UnitsInStock").value.property.value(), tapOrder, row.child("QuantityPerUnit").value.property.value());
		}
	}
}
